package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Main {

    public static void main(String[] args) {
        int targetSize;
        if(args.length != 1) {
            throw new IllegalArgumentException("Pricer needs a single command line argument");
        }
        try {
            targetSize = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Argument " + args[0] + " must be an integer.");
        }
        if(! (targetSize > 0) ) {
            throw new IllegalArgumentException("Argument must be greater than zero.");
        }
        Book marketBook = new Book();
        String currentSellIncome = "NA";
        String currentBuyExpense = "NA";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = reader.readLine();
                if(input != null) {
                    if( ! isValidMessage(input) )  {
                        System.err.println("malformed input message " + input);
                    } else if ( getActionFromMessage(input).equals("reduce") && (! marketBook.containsOrderID(getOrderIDFromMessage(input)) ) ) {
                        System.err.println("invalid reduce message " + input);
                    } else {
                        String side = getSideFromMessage(marketBook, input);
                        updateBook(input, marketBook);
                        if(side.equals("sell")) {
                            String newValue = marketBook.completeOrder("buy", targetSize);
                            if(! newValue.equals(currentBuyExpense)){
                                System.out.println(getTimestampFromMessage(input) + " B " + newValue);
                                currentBuyExpense = newValue;
                            }
                        } else {
                            String newValue = marketBook.completeOrder("sell", targetSize);
                            if(! newValue.equals(currentSellIncome)){
                                System.out.println(getTimestampFromMessage(input) + " S " + newValue);
                                currentSellIncome = newValue;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // use the raw access of split array vs using the other helper methods, here in the isValid method
    public static boolean isValidMessage(String msg) {
        if(msg == null || msg.length() == 0) {
            return false;
        }
        String[] messageComponents = splitMessage(msg);
        if(messageComponents.length < 4) {
            return false;
        }
        if( ! (messageComponents[1].equals("A") || messageComponents[1].equals("R")) ) {
            return false;
        }
        if(messageComponents[1].equals("A")) {
            if( messageComponents.length != 6
                || ( ! (messageComponents[3].equals("S") || messageComponents[3].equals("B")) ) ) {
                return false;
            }
            try {
                Integer.parseInt(messageComponents[5]);
                Integer.parseInt((messageComponents[4].replace(".", "")));
            } catch(NumberFormatException e) {
                return false;
            }
        } else {
            if( messageComponents.length != 4 ) {
                return false;
            }
            try {
                Integer.parseInt(messageComponents[3]);
            } catch(NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static String[] splitMessage(String msg) {
        return msg.split("\\s");
    }

    public static String getTimestampFromMessage(String msg) {
        String timestamp = splitMessage(msg)[0];
        return timestamp;
    }

    public static String getOrderIDFromMessage(String msg) {
        String orderID = splitMessage(msg)[2];
        return orderID;
    }

    public static String getActionFromMessage(String msg) {
        String action = splitMessage(msg)[1];
        return action.equals("A") ? "add" : "reduce";
    }

    public static String getSideFromMessage(Book book, String msg) {
        if(getActionFromMessage(msg).equals("add")) {
            return splitMessage(msg)[3].equals("B") ? "buy" : "sell";
        } else {
            return book.getSideByOrderID(getOrderIDFromMessage(msg));
        }
    }

    public static void updateBook(String msg, Book book) {
        String[] messageComponents = splitMessage(msg);
        if(getActionFromMessage(msg).equals("add")) {
            String orderID = getOrderIDFromMessage(msg);
            int size = Integer.parseInt(messageComponents[5]);
            String side = getSideFromMessage(book, msg);
            int priceInCents = Integer.parseInt((messageComponents[4].replace(".", "")));
            book.addOrder(orderID, side, priceInCents, size);
        } else {
            String orderID = getOrderIDFromMessage(msg);
            int reduction = Integer.parseInt(messageComponents[3]);
            book.reduceOrder(orderID, reduction);
        }
    }
}