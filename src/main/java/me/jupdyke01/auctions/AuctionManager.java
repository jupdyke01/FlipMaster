package me.jupdyke01.auctions;

import com.google.gson.*;
import me.jupdyke01.FlipMaster;
import me.jupdyke01.auctions.objects.AuctionData;
import me.jupdyke01.auctions.objects.BINAuction;
import me.jupdyke01.auctions.objects.PriceData;
import me.jupdyke01.auctions.objects.PriceDataHolder;
import me.jupdyke01.generics.Pair;
import me.jupdyke01.utils.Timer;
import me.jupdyke01.utils.TokenBucket;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class AuctionManager {

    private final FlipMaster main;
    private int minAuctionPrice;
    private int maxAuctionPrice;
    private int margin;
    private long lastUpdated = 0;
    private int volume;
    private double threshold;
    private String auctionStatus = "";
    private PriceDataHolder priceData;
    private final OkHttpClient client;
    private JsonParser jsonParser = new JsonParser();

    public AuctionManager(FlipMaster main) {
        this.main = main;
        this.minAuctionPrice = 1;
        this.maxAuctionPrice = 25000000;
        this.margin = 250000;
        this.volume = 250;
        this.threshold = 1.2;
        client = new OkHttpClient();
        new Thread(this::start).start();
    }

    //Start
    //Populate item prices
    //------------
    //Grab auction house
    //Loop on timer

    public void start() {
        Gson gson = new Gson();
        if (priceData == null) {
            auctionStatus = "Getting Prices";
            Timer price = new Timer("Getting Price Data");
            price.start();
            try {
                updatePriceData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            price.stop();
            price.display();
        }

        if (main.status.equalsIgnoreCase("on")) {
            //Fetch all auction house pages as JSON
            Timer populate = new Timer("Get Auction Pages");
            populate.start();
            List<String> auctionPages = getAuctionHouse();
            populate.stop();
            populate.display();


            //Get all auction items from auction pages
            Timer item = new Timer("Get Auction Items");
            item.start();
            ConcurrentHashMap<String, AuctionData> unsortedItems = getAuctionItems(auctionPages);
            item.stop();
            item.display();

            Timer sort = new Timer("Sort Auction Items");
            sort.start();
            ConcurrentHashMap<String, AuctionData> items = sortAuctionItems(unsortedItems);
            sort.stop();
            sort.display();
            main.getAuctionWindow().updateItems(items);

            auctionPages.clear();
            unsortedItems.clear();
            items.clear();

            lastUpdated = jsonParser.parse(fetchAuctions(0)).getAsJsonObject().get("lastUpdated").getAsLong();
            long sleepTime = (60 - getAge()) * 1000L;
            if (sleepTime > 0) {
                this.auctionStatus = "sleep";
                main.getAuctionWindow().updateSleep(sleepTime);
                try {
                    Thread.sleep(sleepTime);
                } catch(InterruptedException ignored){};
            }

            long currentUpdated = jsonParser.parse(fetchAuctions(0)).getAsJsonObject().get("lastUpdated").getAsLong();
            while (currentUpdated == lastUpdated) {
                try {
                    Thread.sleep(50);
                    currentUpdated = jsonParser.parse(fetchAuctions(0)).getAsJsonObject().get("lastUpdated").getAsLong();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            start();
        }
    }

    //Happens one time at initialization
    public void updatePriceData() throws IOException {
        auctionStatus = "Setting Up";
        String filePath = "item_prices.json";
        File priceFile = new File(filePath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        PriceDataHolder priceHolder;

        if (priceFile.exists()) {
            FileReader reader = new FileReader(priceFile);
            priceHolder = gson.fromJson(reader, PriceDataHolder.class);
            reader.close();
            if (priceHolder != null) {
                if (System.currentTimeMillis() - priceHolder.getLastUpdated() < 259200000) {
                    priceData = priceHolder;
                    return;
                }
            }
        }

        ArrayList<String> itemsToCheck = new ArrayList<>();
        JsonArray array = jsonParser.parse(fetchItems()).getAsJsonArray();
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            String tag = object.get("tag").getAsString();
            String flags = object.get("flags").getAsString();
            if (!object.get("name").isJsonNull()) {
                String name = object.get("name").getAsString();
                if (name.equalsIgnoreCase("null")) {
                    continue;
                }
                if (name.startsWith("PET")) {
                    continue;
                }
            }
            if (!flags.contains("AUCTION")) {
                continue;
            }
            itemsToCheck.add(tag);
        }

        HashMap<String, PriceData> loadingPriceInfo = new HashMap<>();
        TokenBucket bucket = new TokenBucket(10);
        int size = itemsToCheck.size();
        for (int i = 0; i < size; i++) {
            bucket.consumeToken();
            Pair<Integer, Double> pInfo = getPriceInfo(itemsToCheck.get(i));
            loadingPriceInfo.put(itemsToCheck.get(i), new PriceData(pInfo.getFirst(), pInfo.getSecond()));
            double percent = Math.round((i * 1.0 / size) * 100);
            int itemsLeft = size - i;
            int timeLeft = (itemsLeft / 10) * 6;
            int count = i + 1;
            if (count % 10 == 0) {
                System.out.println("Amount Done: " + count + "/" + size);
                System.out.println("Percent: " + percent + "%");
                System.out.println("Time Left: " + timeLeft + " seconds");
            }
        }

        priceHolder = new PriceDataHolder(System.currentTimeMillis(), loadingPriceInfo);
        FileWriter fis = new FileWriter(priceFile);
        gson.toJson(priceHolder, fis);
        fis.flush();
        fis.close();
        this.priceData = priceHolder;
    }

    //Happens first
    public List<String> getAuctionHouse() {
        auctionStatus = "Populating";
        List<String> auctionHouseData = Collections.synchronizedList(new ArrayList<>());
        int pagesRequired = new JsonParser().parse(fetchAuctions(0)).getAsJsonObject().get("totalPages").getAsInt();

        ExecutorService executor = Executors.newFixedThreadPool(pagesRequired);
        List<CompletableFuture<String>> pages = new ArrayList<>();
        for (int i = 0; i < pagesRequired; i++) {
            int page = i;
            pages.add( CompletableFuture.supplyAsync(() -> {
                auctionHouseData.add(fetchAuctions(page));
                return "";
            }, executor));
        }
        pages.forEach(CompletableFuture::join);
        executor.shutdownNow();
        return auctionHouseData;
    }

    //Happens second
    public ConcurrentHashMap<String, AuctionData> getAuctionItems(List<String> auctionPages) {
        ConcurrentHashMap<String, AuctionData> items = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(auctionPages.size());
        List<CompletableFuture<String>> pages = new ArrayList<>();
        for (String page : auctionPages) {
            pages.add(CompletableFuture.supplyAsync(() -> {
                JsonObject auctionHouse = jsonParser.parse(page).getAsJsonObject();
                JsonArray auctions = auctionHouse.getAsJsonArray("auctions");
                for (JsonElement auction : auctions) {
                    Pair<String, BINAuction> bin = parseAuction(auction);
                    if (bin == null) {
                        continue;
                    } else {
                        String id = bin.getFirst();
                        if (!items.containsKey(id)) {
                            AuctionData auctionData = new AuctionData();
                            auctionData.addAuction(bin.getSecond());
                            items.put(id, auctionData);
                        } else {
                            items.get(id).addAuction(bin.getSecond());
                        }
                    }
                }
                return "";
            }, executor));
        }
        pages.forEach(CompletableFuture::join);
        executor.shutdownNow();
        return items;
    }


    //Happens third
    public ConcurrentHashMap<String, AuctionData> sortAuctionItems(ConcurrentHashMap<String, AuctionData> items) {
        List<String> removeItems = new ArrayList<>();
        for (String item : items.keySet()) {
            AuctionData data = items.get(item);
            if (data.getSecond() == null) {
                removeItems.add(item);
                continue;
            }
            data.sortAuctions();
            int lowestPrice = data.getFirst().getPrice();
            int secondLowestPrice = data.getSecond().getPrice();

            if (lowestPrice < minAuctionPrice || lowestPrice > maxAuctionPrice) {
                removeItems.add(item);
                continue;
            }

            if (priceData.getItems().get(item).getMedianPrice() * 1.2 < lowestPrice) {
                removeItems.add(item);
                continue;
            }

            if (priceData.getItems().get(item).getVolume() < volume) {
                removeItems.add(item);
                continue;
            }

            if (secondLowestPrice - lowestPrice < margin) {
                removeItems.add(item);
                continue;
            }
        }

        removeItems.forEach(items::remove);
        removeItems.clear();

        return items;
    }

    public Pair<String, BINAuction> parseAuction(JsonElement auction) {
        JsonObject auctionObject = auction.getAsJsonObject();
        boolean bin = auctionObject.get("bin").getAsBoolean();
        boolean claimed = auctionObject.get("claimed").getAsBoolean();
        String auctionId = auctionObject.get("uuid").getAsString();
        String data = auctionObject.get("item_bytes").getAsString();
        Pair<String, Integer> nbtData = nbtParser(data);
        String id = nbtData.getFirst();
        int count = nbtData.getSecond();
        int price = auctionObject.get("starting_bid").getAsInt() / count;
        if (!bin) {
            return null;
        }
        if (claimed) {
            return null;
        }
        if (price < 0) {
            return null;
        }
        if (!priceData.getItems().containsKey(id)) {
            return null;
        }
        return new Pair<>(id, new BINAuction(auctionId, price));
    }

    public Pair<Integer, Double> getPriceInfo(String itemId) {
        String priceString = fetchPriceInfo(itemId);
        if (priceString == null || priceString.equalsIgnoreCase("")) {
            return new Pair<>(0,0.0);
        }
        JsonObject priceJson;

        try {
            priceJson = jsonParser.parse(priceString).getAsJsonObject();
        } catch (JsonParseException e) {
            return new Pair<>(0,0.0);
        }
        int median = 0;
        double volume = 0.0;
        if (priceJson.has("median")) {
            median = priceJson.get("median").getAsInt();
        } else {
            System.out.println(itemId);
        }
        if (priceJson.has("volume")) {
            volume = priceJson.get("volume").getAsDouble();
        } else {
            System.out.println(itemId);
        }
        return new Pair<>(median, volume);
    }

    public String fetchAuctions(int page) {
        String url = "https://api.hypixel.net/skyblock/auctions?page=" + page;
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            response.close();
            return result;
        } catch (IOException ignored) {}
        return "";
    }

    public String fetchItems() {
        String url = "https://sky.coflnet.com/api/items";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            String result;
            if (response.isSuccessful()) {
                result = response.body().string();
            } else {
                result = "";
            }
            response.close();
            return result;
        } catch (IOException ignored) {}
        return "";
    }

    public String fetchPriceInfo(String itemId) {
        String url = "https://sky.coflnet.com/api/item/price/" + itemId;
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            response.close();
            return result;
        } catch (IOException ignored) {}
        return "";
    }

    public int getAge() {
        String url = "https://api.hypixel.net/skyblock/auctions?page=0";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            int age = Integer.parseInt(response.header("age"));
            response.close();
            return age;
        } catch (IOException ignored) {}
        return 0;
    }

    public Pair<String, Integer> nbtParser(String nbtStr) {
        try {
            NBTCompound compound = NBTReader.readBase64(nbtStr);
            NBTList items = compound.getList("i");
            for (int i = 0; i < items.size(); i++) {
                NBTCompound item = items.getCompound(i);
                NBTCompound extraAttributes = item.getCompound("tag").getCompound("ExtraAttributes");
                String id = extraAttributes.getString("id");
                int count = item.getInt("Count", 1);
                return new Pair<String, Integer>(id.replaceAll("STARRED_", ""), count);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Pair<String, Integer>("", 1);
    }

    public int getMinAuctionPrice() {
        return minAuctionPrice;
    }

    public void setMinAuctionPrice(int minAuctionPrice) {
        this.minAuctionPrice = minAuctionPrice;
    }

    public int getMaxAuctionPrice() {
        return maxAuctionPrice;
    }

    public void setMaxAuctionPrice(int maxAuctionPrice) {
        this.maxAuctionPrice = maxAuctionPrice;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getAuctionStatus() {
        return auctionStatus;
    }
}

