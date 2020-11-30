package com.steve.cloudatlas.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steve.cloudatlas.external.TwitchClient;
import com.steve.cloudatlas.external.TwitchException;


@WebServlet(name = "GameServlet", urlPatterns = {"/game"})  // 如果不写urlPattern在url中应该写ClassName：GameServlet
public class GameServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // response.getWriter().print("Hello World!");

        // 解析 URL 里的 gamename parameter
        // String gamename = request.getParameter("gamename");
        // request.getParameter can only read parameter in url
        // request.getParameter can only be used in doGet method
        // response.getWriter().print("Game is: " + gamename);

        // Method 1:
        // 返回 JSON 格式的数据给前端
//        response.setContentType("application/json");
//        JSONObject obj = new JSONObject();
//        obj.put("name", "World of Warcraft");
//        obj.put("developer", "Blizzard Entertainment");
//        obj.put("release_time", "Feb 11, 2005");
//        obj.put("website", "https://www.worldofwarcraft.com");
//        obj.put("price", 49.99);
//        response.getWriter().print(obj);  // 隐含调用了 game.toString()

        // Method 2:
        // 把Java格式的对象 convert成JSONObject的数据
//        Game game = new Game("World of Warcraft", "Blizzard Entertainment",
//                "Feb 11, 2005", "https://www.worldofwarcraft.com", 49.99);
//        // Jackson library 里的 ObjectMapper class 提供了转化 Java Object
//        ObjectMapper mapper = new ObjectMapper();
//        response.getWriter().print(mapper.writeValueAsString(game));

        String gameName = request.getParameter("game_name");
        TwitchClient client = new TwitchClient();

        response.setContentType("application/json;charset=UTF-8");
        try {
            if (gameName != null) {
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.searchGames(gameName)));
            } else {
                // limit = 0 代表 default limit 20
                response.getWriter().print(new ObjectMapper().writeValueAsString(client.topGames(0)));
            }
        } catch (TwitchException e) {
            throw new ServletException(e);
        }
    }
}
