package ru.bot;


import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.heroku.api.Dyno;
import com.heroku.api.Heroku;
import com.heroku.api.HerokuAPI;
import com.heroku.sdk.deploy.util.HerokuCli;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.*;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;



import java.sql.*;

public class Bot {
    public static void main(String[] args) throws ClientException, ApiException, InterruptedException {

        TransportClient tranClien = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(tranClien);
        Random random = new Random();
        GroupActor actor = new GroupActor(208573536, "da19a8dd6b30bb1c3999d55c665fa48efa3fd9b5438f2b79c2283785b123f02844392fef518e56f6fe395");
        Integer ts =  vk.messages().getLongPollServer(actor).execute().getTs();

        Bot bot = new Bot();
        //bot.OpenSQL();


        new Pinger("Start").start();
        new Restarter("Start").start();

        while (true) {
            MessagesGetLongPollHistoryQuery historyQuery = vk.messages().getLongPollHistory(actor).ts(ts);
            List<Message> messages = historyQuery.execute().getMessages().getItems();
            if (!messages.isEmpty()) {
                messages.forEach(message -> {
                    System.out.println(message.toString());
                    new MessageListener(message).start();

                });
            }
            ts = vk.messages().getLongPollServer(actor).execute().getTs();
            Thread.sleep(500);
        }
    }

    Connection co;

    void OpenSQL() {
        try {
            Class.forName("org.postgresql.Driver");
            String urlB = "jdbc:postgresql://ec2-52-208-229-228.eu-west-1.compute.amazonaws.com:5432/d7qp2lfbdpkph0?sslmode=require";
            System.out.println("Connection");
            co = DriverManager.getConnection(urlB, "yzqcxjfxejhqjj", "4c1c36f0bd7bda77c936cf8e31b52ee576a8dc507a4e987da7ac124eccd977a1");
            System.out.println("Connected");
        }catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    boolean InsertSQL(int vkid) {
        Bot bot = new Bot();
        bot.OpenSQL();
        String query = "INSERT INTO users (vkid) VALUES ("+ vkid +");";
        try {
            Statement statem = co.createStatement();
            statem.executeUpdate(query);
            System.out.println("???????????? ??????????????????: " + vkid);
            statem.close();
            bot.CloseSQL();
            return true;
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        //bot.CloseSQL();
    }

    boolean SerchSQL(int namevk) {
        Bot bot = new Bot();

        try {
            bot.OpenSQL();
            //bot.OpenSQL();
            Statement statem = co.createStatement();
            String query = "SELECT vkid FROM users";
            ResultSet rs = statem.executeQuery(query);
            //System.out.println("HUI1");
            while (rs.next()) {
                //int id = rs.getInt("id");
                int name = rs.getInt("vkid");
                //String nikname = rs.getString("nikname");
                if (namevk == name) {
                    statem.close();
                    bot.CloseSQL();
                    return true;
                }
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
            //System.out.println("HUI2");
        }
        //System.out.println("HIU");
        bot.CloseSQL();
        return false;
    }

    void CloseSQL() {
        try {
            co.close();
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}

class Restarter extends Thread {
    String message;

    public Restarter (String messag) {
        message = messag;
    }

    public void run () {
        while (true) {
            try {
                Thread.sleep(3600000);
                HerokuAPI heroku = new HerokuAPI("6ddea597-cb42-45ec-8a6e-92c8008340e7");
                heroku.restartDyno("vkbotfc", "worker.1");
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}

class Pinger extends Thread {
    String message;

    public Pinger (String messag) {
        message = messag;
    }

    public void run () {
        while (true) {
            try {
                Document doc;
                doc = Jsoup.connect("https://www.google.com/").get();
                String site = doc.text();
                System.out.println("Pinged Google");
                //site = "";
                Thread.sleep(10000);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}

class MessageListener extends Thread {
    Message message;

    public MessageListener (Message messag) {
        message = messag;
    }

    public void run () {
        Bot bot = new Bot();
        TransportClient tranClien = new HttpTransportClient();
        VkApiClient vk = new VkApiClient(tranClien);
        Random random = new Random();
        GroupActor actor = new GroupActor(208573536, "da19a8dd6b30bb1c3999d55c665fa48efa3fd9b5438f2b79c2283785b123f02844392fef518e56f6fe395");
        try {
            Integer ts =  vk.messages().getLongPollServer(actor).execute().getTs();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        try {
            bot.OpenSQL();
            if (message.getText().contains("!")){
                if (bot.SerchSQL(message.getFromId()) && message.getFromId() != -208573536) {
                    if (message.getText().contains("!add") && message.getFromId() == 263889683) {
                        String text = message.getText();
                        String text1 = text.replaceAll("[!add, \\s+]", "");
                        boolean request = bot.InsertSQL(Integer.parseInt(text1));
                        if (request){
                            vk.messages().send(actor).message("???????????? ??????????????????: " + text1).userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                        }else {
                            vk.messages().send(actor).message("Error").userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                        }
                    }

                    else if (message.getText().contains("!????????????????????")) {
                        Document doc;
                        try {
                            doc = Jsoup.connect("https://raw.githubusercontent.com/VERSUS-BotDC/urlupdater/main/urlupdater").get();
                            String UrlFK = doc.text();
                            doc = Jsoup.connect("https://raw.githubusercontent.com/VERSUS-BotDC/urlupdater/main/version").get();
                            String Version = doc.text();
                            vk.messages().send(actor).message("?????????????????? ???????????????????? "+Version+"\uD83D\uDE0F \n\n" +
                                    "????????????: " +UrlFK+"\n\n"+
                                    "???????? ?????????? ?????????? ???????????????????? ???????????? \n\"!???????????? (???????????? ????????????????????)\" \uD83E\uDD73").userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                        }catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                    }else if (message.getText().contains("!????????????")) {
                        String text = message.getText().replaceAll("[!????????????, \\s+]", "");
                        vk.messages().send(actor).message("?????????????????? ???? ???????????????????? ???????? ???????????????????? ????????????????????????????! \uD83E\uDD73").userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                        vk.messages().send(actor).message("?????????? ????????????????????: "+ text + ". ??????????????????????: "+ message.getFromId()).userId(263889683).randomId(random.nextInt(10000)).execute();
                    }
                    else if (message.getText().contains("!id")) {
                        vk.messages().send(actor).message("?????? id: " + message.getFromId()).userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                    }

                }
                else if (!bot.SerchSQL(message.getFromId()) && message.getFromId() != -208573536){
                    vk.messages().send(actor).message("???? ???? ???????????????? FastConnect!\uD83D\uDE09\n\n" +
                            "?????? ?????????????? ???????????????? \"???????? ???????????? ????\".\uD83D\uDE11 \n?? ???????????????? ???????????? ????????????????????????????.\uD83D\uDE05 \n\n" +
                            "?????????????????? FastConnect 75?? \uD83E\uDD29").userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                }

            }else {
                if (message.getText().equals("????????????")) {
                    vk.messages().send(actor).message("??????????????????????! ???????????????? ???????? ???????????? ?? ???????????????? ?????????? ????????????????????????????!\uD83E\uDD29").userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                }else if (message.getText().contains(".id")) {
                    vk.messages().send(actor).message("?????? id: " + message.getFromId()).userId(message.getFromId()).randomId(random.nextInt(10000)).execute();
                }
            }
            bot.CloseSQL();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
