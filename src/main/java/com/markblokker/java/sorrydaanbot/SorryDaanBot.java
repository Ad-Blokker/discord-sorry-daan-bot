package com.markblokker.java.sorrydaanbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.annotation.Nonnull;
import java.io.*;


public class SorryDaanBot implements EventListener {

    private static int sorryDaanCount = 0;

    public static void readDataFromDisk(){
        try {
            InputStream fileInput = new FileInputStream("sorrydaancount.txt");
            BufferedReader buf = new BufferedReader(new InputStreamReader(fileInput));

            String sorryDaanCountString = buf.readLine();
            sorryDaanCount = Integer.parseInt(sorryDaanCountString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveDataToDisk(){
        try (PrintWriter out = new PrintWriter("sorrydaancount.txt")) {
            out.println(sorryDaanCount);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void messageHandeler(Message message, MessageReceivedEvent event){

        if (message.getContentRaw().contains("-")){
            return;
        }
        if (message.getContentRaw().contains("totaal") || message.getContentRaw().contains("total") || message.getContentRaw().contains("count")) {
            MessageAction action = event.getChannel().sendMessage("Het totaal aantal excuses aan Daan is: **" + sorryDaanCount + "**");
            action.submit();
            return;
        }
        if (message.getContentRaw().contains("+")) {
            System.out.println("Adding to counter");
            String toBeSplitted = message.getContentRaw();
            String[] splittedPlus = toBeSplitted.split("\\+");
            String[] splitted = splittedPlus[1].split(" ");
            int toBeAdded = Integer.parseInt(splitted[0]);

            if (toBeAdded > 10){
                return;
            }

            System.out.println("Adding " + toBeAdded + " to counter (-1 because 1 will be added anyways after this" +
                    ", previous value (" + sorryDaanCount + ")");
            sorryDaanCount += toBeAdded;
        } else {
            System.out.println("Adding 1 to counter, previous value (" + sorryDaanCount + ")");
            sorryDaanCount += 1;
        }
    }


    @Override
    public void onEvent(@Nonnull GenericEvent genericEvent) {
        if (genericEvent instanceof ReadyEvent) {
            System.out.println("SorryDaanBot has been activated.");
        }

        if (!(genericEvent instanceof MessageReceivedEvent)) {
            return;
        }

        MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
        Message message = event.getMessage();
        System.out.printf("Message received from '%s': '%s'%n", message.getAuthor().getAsTag(), message);

        if (message.isMentioned(event.getJDA().getSelfUser())) {

            readDataFromDisk();
            messageHandeler(message, event);
            saveDataToDisk();

            System.out.println("New counter value is: " + sorryDaanCount);
            genericEvent.getJDA().getPresence().setActivity(Activity.watching("Sorry totaal: " + sorryDaanCount));

        } else {
            return;
        }
    }

    public static void main(String[] args) {

        readDataFromDisk();

        try {
            // create instance
            SorryDaanBot instance = new SorryDaanBot();

            // make connection to Discord API
            JDABuilder builder = new JDABuilder(args[0])
                    .setActivity(Activity.watching("Sorry totaal: " + sorryDaanCount))
                    .addEventListeners(instance);
            JDA jda = builder.build();

            // wait until JDA has been initialized
            jda.awaitReady();

            System.out.printf("Connected to the following servers: %s%n", jda.getGuilds());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
