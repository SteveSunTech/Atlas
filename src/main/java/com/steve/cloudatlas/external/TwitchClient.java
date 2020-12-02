package com.steve.cloudatlas.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steve.cloudatlas.entity.Game;
import com.steve.cloudatlas.entity.Item;
import com.steve.cloudatlas.entity.ItemType;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class TwitchClient {
    private static final String TOKEN = "Bearer 07gaukbs3kuheccr2wcvw2knyszoty";
    private static final String CLIENT_ID = "u3g4k0rychdlr9ex6gcvyg3ssa0ywb";
    private static final String TOP_GAME_URL_TEMPLATE = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;
    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    // implement buildGameURL function
    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) {
            // top game
            // https://api.twitch.tv/helix/games/top?first=20
            return String.format(url, limit);
        } else {
            try {
                // amont us => among%20us
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // getGame
            // https://api.twitch.tv/helix/games?name=among%20us
            return String.format(url, gameName);
        }
    }

    private String buildSearchURL(String url, String gameId, int limit) {

        try {
                // amont us => among%20us
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, gameId, limit);
    }


    // implement buildGameURL function
    private String searchTwitch(String url) throws TwitchException {
        // httpclient 用来帮助发送请求
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // responseHandler 用来处理请求 此处用了 lambda expression
        // input: response   output:
        ResponseHandler<String> responseHandler = response -> {
            // get response code
            int responseCode = response.getStatusLine().getStatusCode();
            // unsuccessful
            if (responseCode != 200) {
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }
            // get response body
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new TwitchException("Failed to get result from Twitch API");
            }
            // 把 response body 的整体内容变成一个 JSONObject
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            // 我们需要的信息是 data 这个 key 所对应的 array
            return obj.getJSONArray("data").toString();
        };
        // 用 httpclient 发送请求
        try {
            HttpGet httpGetRequest = new HttpGet(url);
            httpGetRequest.setHeader("Authorization", TOKEN);
            httpGetRequest.setHeader("Client-Id", CLIENT_ID);
            return httpclient.execute(httpGetRequest, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to get result from Twitch API");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // implement getGameList method to convert Twitch return data to a list of Game objects
    private List<Game> getGameList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 把 JSON 格式的array convert 为Java Object
            // 如果无法一一对应会出现JSONException
            // 后面一个参数（Game[].class）的意思是 convert 成 Game 这个 class 组成的 array
            Game[] games = mapper.readValue(data, Game[].class);
            return Arrays.asList(games);
            // return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }

    // 返回当前 popular 的 game
    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        // Step 1:
        String url = buildGameURL(TOP_GAME_URL_TEMPLATE, "", limit);
        // Step 2:
        String responseBody = searchTwitch(url);
        // Step 3:
        return getGameList(responseBody);
        // return getGameList(searchTwitch(buildGameURL(TOP_GAME_URL, "", limit)));
    }


    // 根据 gameName 返回具体内容
    public Game searchGames(String gameName) throws TwitchException {
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }

    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 把 JSON 格式的array convert 为Java Object
            // 如果无法一一对应会出现JSONException
            // 后面一个参数（Game[].class）的意思是 convert 成 Game 这个 class 组成的 array

            return Arrays.asList(mapper.readValue(data, Item[].class));
            // return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }

    private List<Item> searchStreams(String gameId, int limit) throws TwitchException{
        String url = buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit);
        String data = searchTwitch(url);
        List<Item> streams = getItemList(data);
        for(Item item : streams) {
            //这两个twitch不会返回，所以要自己加
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
            item.setType(ItemType.STREAM);
        }
        return streams;
    }

    private List<Item> searchClips(String gameId, int limit) throws TwitchException{
        String url = buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit);
        String data = searchTwitch(url);
        List<Item> clips = getItemList(data);
        for(Item item : clips) {
            //这两个twitch不会返回，所以要自己加
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
            item.setType(ItemType.CLIP);
        }
        return clips;
    }

    private List<Item> searchVideos(String gameId, int limit) throws TwitchException{
        String url = buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit);
        String data = searchTwitch(url);
        List<Item> videos = getItemList(data);
        for(Item item : videos) {
            //这两个twitch不会返回，所以要自己加
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }

    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return itemMap;
    }
}


