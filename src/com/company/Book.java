package com.company;

import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Book {
    static HashMap<String, Order> buyHM, sellHM;
    static int minSellPriceInCents, maxSellPriceInCents, minBuyPriceInCents, maxBuyPriceInCents;
    static int[] sellPriceSize, buyPriceSize;
    // set max potential price for a book's shares at 10k in cents
    // 1000001 so that zero index array for price in cents allows arr[1000000] = size
    static final int MAXIMUM_PRICE_IN_CENTS = 1000000;
    
    Book() {
        buyHM = new HashMap<>();
        sellHM = new HashMap<>();
        minBuyPriceInCents = MAXIMUM_PRICE_IN_CENTS;
        minSellPriceInCents = MAXIMUM_PRICE_IN_CENTS;
        maxBuyPriceInCents = 0;
        maxSellPriceInCents = 0;
        sellPriceSize = new int[MAXIMUM_PRICE_IN_CENTS + 1];
        buyPriceSize = new int[MAXIMUM_PRICE_IN_CENTS + 1];
        for(int i = 0; i < MAXIMUM_PRICE_IN_CENTS + 1; i++) {
            sellPriceSize[i] = 0;
            buyPriceSize[i] = 0;
        }
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

    public static void addOrder(String orderID, String side, int priceInCents, int size) {
        Order newOrder = new Order(orderID, side, priceInCents, size);
        if(side.equals("buy")) {
            buyHM.put(newOrder.orderID, newOrder);
            buyPriceSize[priceInCents] += size;
            if(priceInCents > maxBuyPriceInCents) maxBuyPriceInCents = priceInCents;
            if(priceInCents < minBuyPriceInCents) minBuyPriceInCents = priceInCents;
        } else {
            sellHM.put(newOrder.orderID, newOrder);
            sellPriceSize[priceInCents] += size;
            if(priceInCents > maxSellPriceInCents) maxSellPriceInCents = priceInCents;
            if(priceInCents < minSellPriceInCents) minSellPriceInCents = priceInCents;
        }
    }

    public static void reduceOrder(String orderID, int reduction) {
        HashMap<String, Order> hm = getSideByOrderID(orderID).equals("buy") ? buyHM : sellHM;
        Order order = hm.get(orderID);
        order.size -= reduction;
        if(order.size <= 0) {
            hm.remove(order.orderID);
        }
        if(order.side.equals("buy")) {
            buyPriceSize[order.priceInCents] -= reduction;
            if(buyPriceSize[order.priceInCents] <= 0 ) {
                buyPriceSize[order.priceInCents] = 0;
                if(order.priceInCents == minBuyPriceInCents) {
                    while(! (buyPriceSize[minBuyPriceInCents] > 0) && minBuyPriceInCents != maxBuyPriceInCents) {
                        minBuyPriceInCents++;
                    }
                } else if(order.priceInCents == maxBuyPriceInCents) {
                    while(! (buyPriceSize[maxBuyPriceInCents] > 0) && minBuyPriceInCents != maxBuyPriceInCents) {
                        maxBuyPriceInCents--;
                    }
                }
                if(minBuyPriceInCents == maxBuyPriceInCents && buyPriceSize[minBuyPriceInCents] == 0) {
                    minBuyPriceInCents = MAXIMUM_PRICE_IN_CENTS;
                    maxBuyPriceInCents = 0;
                }
            }
        } else {
            sellPriceSize[order.priceInCents] -= reduction;
            if(sellPriceSize[order.priceInCents] <= 0 ) {
                sellPriceSize[order.priceInCents] = 0;
                if(order.priceInCents == minSellPriceInCents) {
                    while(! (sellPriceSize[minSellPriceInCents] > 0) && minSellPriceInCents != maxSellPriceInCents) {
                        minSellPriceInCents++;
                    }
                } else if(order.priceInCents == maxSellPriceInCents) {
                    while(! (sellPriceSize[maxSellPriceInCents] > 0) && minSellPriceInCents != maxSellPriceInCents) {
                        maxSellPriceInCents--;
                    }
                }
                if(minSellPriceInCents == maxSellPriceInCents && sellPriceSize[minSellPriceInCents] == 0) {
                    minSellPriceInCents = MAXIMUM_PRICE_IN_CENTS;
                    maxSellPriceInCents = 0;
                }
            }
        }
    }

    // completeOrder() will be called after updating book. if new sell order added, we complete buy order.
    // complete order will be on sideOfOrder for us, so us buying, hit sells in sell side of book
    public static String completeOrder(String sideOfOrder, int targetSize) { 

            if(( sideOfOrder.equals("sell") ? maxBuyPriceInCents : maxSellPriceInCents) == 0) {
                return "NA";
            }
            // sideOfOrder buy or sell
            // for buys, we want to hit highest to lowest, as we hit buys, we're selling
            
            // sells, we want to hit lowest to highest, as we hit sells, we're buying
        
            int sharesRemaining = targetSize;
            int valueInCents = 0;
            
            // grab appropriate int[] array, and set increment buys = -1, sells = 1
            int[] array = sideOfOrder.equals("buy") ?  sellPriceSize :  buyPriceSize ;
            int increment = sideOfOrder.equals("buy") ? 1 : -1;
            // set starting price and stopping price for while loop
            int currentPrice = sideOfOrder.equals("buy") ? minSellPriceInCents :  maxBuyPriceInCents ;
            int stopPrice = sideOfOrder.equals("buy") ?  maxSellPriceInCents :  minBuyPriceInCents ;
            int shares = Math.min(sharesRemaining, array[currentPrice]);
            valueInCents += (shares * currentPrice);
            sharesRemaining -= shares;
            while(currentPrice != stopPrice && ! (sharesRemaining <= 0) ) {
                currentPrice += increment;
                shares = Math.min(sharesRemaining, array[currentPrice]);
                valueInCents += (shares * currentPrice);
                sharesRemaining -= shares;
            }
            if(sharesRemaining <= 0) {
                String valueInCentsStr = "" + valueInCents;
                return valueInCentsStr.substring(0, valueInCentsStr.length() - 2) + "." + valueInCentsStr.substring(valueInCentsStr.length() - 2);
            } else {
                return "NA";
            }

        
//        PriorityQueue<Order> pq;
//        Iterator<Order> iterator;
//        if(sideOfOrder.equals("buy")) {
//            // buy orders are sorted highest to lowest
//            // completing a buy order means hitting the book's sell orders, lowest to highest
//            pq = new PriorityQueue<Order>(10, (Order a, Order b) -> (int) Math.signum(a.priceInCents - b.priceInCents));
//            iterator = sellHM.values().iterator();
//        } else {
//            // sell orders are sorted lowest to highest
//            // completing a sell order means hitting the book's buy orders, highest to lowest
//            pq = new PriorityQueue<Order>(10, (Order a, Order b) -> (int) Math.signum(b.priceInCents - a.priceInCents));
//            iterator = buyHM.values().iterator();
//        }
//        while(iterator.hasNext()) {
//            pq.add(iterator.next());
//        }
//        int sharesRemaining = targetSize;
//        int value = 0;
//        while(sharesRemaining > 0 && ! pq.isEmpty()) {
//            Order order = pq.poll();
//            int hitShares = Math.min(sharesRemaining, order.size);
//            value += order.priceInCents  * hitShares;
//            sharesRemaining -= order.size;
//        }
//        if(sharesRemaining > 0) {
//            // if there are still sharesRemaining, can't complete order
//            return "NA";
//        }
//        String valueInCents = "" + value;
//
//        return valueInCents.substring(0, valueInCents.length() - 2) + "." + valueInCents.substring(valueInCents.length() - 2);
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