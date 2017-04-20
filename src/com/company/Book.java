package com.company;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Book {
    static HashMap<String, Order> buyHM, sellHM;

    Book() {
        buyHM = new HashMap<>();
        sellHM = new HashMap<>();
    }

    public static String getSideByOrderID(String orderID) {
        String side;
        if(buyHM.containsKey(orderID)) {
            side = "buy";
        } else {
            side = "sell";
        }
        return side;
    }

    public static boolean containsOrderID(String orderID) {
        return buyHM.containsKey(orderID) || sellHM.containsKey(orderID);
    }

    public static int getSizeByOrderID(String orderID) {
        return (getSideByOrderID(orderID).equals("buy") ? buyHM : sellHM).get(orderID).size;
    }

    public static void addOrder(String orderID, String side, int price, int size) {
        Order newOrder = new Order(orderID, side, price, size);
        if(side.equals("buy")) {
            buyHM.put(newOrder.orderID, newOrder);
        } else {
            sellHM.put(newOrder.orderID, newOrder);
        }
    }

    public static void reduceOrder(String orderID, int reduction) {
        HashMap<String, Order> hm = getSideByOrderID(orderID).equals("buy") ? buyHM : sellHM;
        Order order = hm.get(orderID);
        order.size -= reduction;
        if(order.size <= 0) {
            hm.remove(order.orderID);
        }
    }

    public static String completeOrder(String sideOfOrder, int targetSize) {
        PriorityQueue<Order> pq;
        Iterator<Order> iterator;
        if(sideOfOrder.equals("buy")) {
            // buy orders are sorted highest to lowest
            // completing a buy order means hitting the book's sell orders, lowest to highest
            pq = new PriorityQueue<Order>(10, (Order a, Order b) -> (int) Math.signum(a.priceInCents - b.priceInCents));
            iterator = sellHM.values().iterator();
        } else {
            // sell orders are sorted lowest to highest
            // completing a sell order means hitting the book's buy orders, highest to lowest
            pq = new PriorityQueue<Order>(10, (Order a, Order b) -> (int) Math.signum(b.priceInCents - a.priceInCents));
            iterator = buyHM.values().iterator();
        }
        while(iterator.hasNext()) {
            pq.add(iterator.next());
        }
        int sharesRemaining = targetSize;
        BigDecimal value = new BigDecimal(0);
        while(sharesRemaining > 0 && ! pq.isEmpty()) {
            Order order = pq.poll();
            int hitShares = Math.min(sharesRemaining, order.size);
            value = value.add(BigDecimal.valueOf(order.priceInCents).multiply(BigDecimal.valueOf(hitShares)));
            sharesRemaining -= order.size;
        }
        if(sharesRemaining > 0) {
            // if there are still sharesRemaining, can't complete order
            return "NA";
        }
        String valueInCents = value.toPlainString();

        return valueInCents.substring(0, valueInCents.length() - 2) + "." + valueInCents.substring(valueInCents.length() - 2);
    }
}

class Order {
    int size;
    int priceInCents;
    String orderID;
    String side;

    public Order(String orderID, String side, int priceInCents, int size) {
        this.side = side;
        this.orderID = orderID;
        this.size = size;
        this.priceInCents = priceInCents;
    }
}