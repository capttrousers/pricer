package com.company;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by sam on 4/16/17.
 */
public class MarketTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setIn(null);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();



    @Test
    public void checksValidAddMessageTest() {
        Assert.assertTrue(Main.isValidMessage("28800538 A b S 44.26 100"));
        Assert.assertTrue(Main.isValidMessage("28800538 A b B 44.26 100"));
    }

    @Test
    public void checksInvalidAddMessageTest() {
        Assert.assertTrue( ! Main.isValidMessage("28800538 A b S"));
        Assert.assertTrue( ! Main.isValidMessage("28800538 A b"));
        Assert.assertTrue( ! Main.isValidMessage("28800538 A b g"));
        Assert.assertTrue( ! Main.isValidMessage("28800538 d b S"));
        Assert.assertTrue( ! Main.isValidMessage("28800538 A b S priceInCents"));
        Assert.assertTrue( ! Main.isValidMessage("28800538 A b S priceInCents size "));
        Assert.assertTrue( ! Main.isValidMessage("28800538 A b S priceInCents 100 "));
        Assert.assertTrue( ! Main.isValidMessage("28800538 A b S priceInCents 100 "));
    }

    @Test
    public void checksEmptyMessageTest() {
        Assert.assertTrue( ! Main.isValidMessage(""));
    }

    @Test
    public void checksNullMessageTest() {
        Assert.assertTrue( ! Main.isValidMessage(null));
    }

    @Test
    public void checksValidReduceMessageTest() {
        Assert.assertTrue(Main.isValidMessage("28800744 R b 100"));
    }
    @Test
    public void checksInvalidReduceMessageTest() {
        Assert.assertTrue( ! Main.isValidMessage("28800744 R b "));
        Assert.assertTrue( ! Main.isValidMessage("28800744 R   "));
        Assert.assertTrue( ! Main.isValidMessage("28800744 R b size "));
        Book book = new Book();
        Assert.assertTrue( ! book.containsOrderID("b"));
    }

    @Test
    public void getsTimestampFromMarketMessageTest() {
        Assert.assertEquals( "28800538", Main.getTimestampFromMessage("28800538 A b S 44.26 100"));
    }

    @Test
    public void getsOrderIDFromMarketMessageTest() {
        Assert.assertEquals( "b", Main.getOrderIDFromMessage("28800538 A b S 44.26 100"));
    }

    @Test
    public void getsActionFromMarketMessageTest() {
        Assert.assertEquals( "add", Main.getActionFromMessage("28800538 A b S 44.26 100"));
        Assert.assertEquals( "reduce", Main.getActionFromMessage("28800744 R b 100"));
    }

    @Test
    public void getsSideByOrderID() {
        Book book = new Book();
        book.addOrder("abc", "sell", 4500, 200);
        Assert.assertEquals("sell", book.getSideByOrderID("abc") );
    }

    @Test
    public void reducesOrder() {
        Book book = new Book();
        book.addOrder("abc", "sell", 4500, 200);
        book.reduceOrder("abc", 100);
        book.addOrder("xyz", "buy", 4000, 100);
        book.reduceOrder("xyz", 10);
        Assert.assertEquals(100, book.getSizeByOrderID("abc") );
        Assert.assertEquals(90, book.getSizeByOrderID("xyz") );
    }

    @Test
    public void updatesBookWithDecimalStrings() {
        Book book = new Book();
        String msg1 = "28800538 A b S 44.26 200";
        String msg2 = "28800744 R b 100";
        Main.updateBook(msg1, book);
        Main.updateBook(msg2, book);
        Assert.assertEquals(100, book.getSizeByOrderID("b") );
        Assert.assertEquals("sell", book.getSideByOrderID("b") );
        String msg3 = "28800538 A abc S 44.67 20";
        String msg4 = "28800538 A xyz S 43.96 81";
        Main.updateBook(msg3, book);
        Main.updateBook(msg4, book);
        Assert.assertEquals("8835.49", book.completeOrder("buy", 200));
    }

    @Test
    public void updatesBook() {
        Book book = new Book();
        String msg1 = "28800538 A b S 44.26 200";
        String msg2 = "28800744 R b 100";
        Main.updateBook(msg1, book);
        Main.updateBook(msg2, book);
        Assert.assertEquals(100, book.getSizeByOrderID("b") );
        Assert.assertEquals("sell", book.getSideByOrderID("b") );
        String msg3 = "28800538 A abc S 44.67 20";
        String msg4 = "28800538 A xyz S 43.96 81";
        Main.updateBook(msg3, book);
        Main.updateBook(msg4, book);
        Assert.assertEquals("8835.49", book.completeOrder("buy", 200));
    }

    @Test
    public void getsSizeByOrderID() {
        Book book = new Book();
        book.addOrder("abc", "sell", 4500, 200);
        book.reduceOrder("abc", 100);
        Assert.assertEquals(100, book.getSizeByOrderID("abc") );
    }

    @Test
    public void completesSellOrderReturnsValueTest() {
        Book book = new Book();
        book.addOrder("abc", "buy", 4587, 20);
        book.addOrder("xyz", "buy", 4410, 60);
        book.addOrder("bgj", "buy", 4550, 40);
        String value = book.completeOrder("sell", 100);
        Assert.assertEquals("4501.40", value);
    }

    @Test
    public void completesBuyOrderReturnsValueTest() {
        Book book = new Book();
        book.addOrder("abc", "sell", 4500, 20);
        book.addOrder("xyz", "sell", 4400, 60);
        book.addOrder("bgj", "sell", 4550, 40);
        String value = book.completeOrder("buy", 100);
        Assert.assertEquals("4450.00", value);
    }

    @Test
    public void completeSellOrderFailsAndReturnsZeroTest() {
        Book book = new Book();
        book.addOrder("abc", "sell", 4500, 50);
        String value = book.completeOrder("sell", 100);
        Assert.assertEquals("NA", value);
    }

    @Test
    public void completeBuyOrderFailsAndReturnsZeroTest() {
        Book book = new Book();
        book.addOrder("abc", "buy", 4500, 50);
        String value = book.completeOrder("buy", 100);
        Assert.assertEquals("NA", value);
    }

//    @Test
//    public void validInputTest() {
//        String[] input = {"20"};
//        Main.main(input);
//        Assert.assertEquals("will run pricer with target size of 20", outContent.toString().trim());
//    }

    @Test
    public void zeroInputTest() throws Exception {
        String[] input = {"0"};
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument must be greater than zero.");
        Main.main(input);
    }

    @Test
    public void noInputTest() throws Exception {
        String[] input = {};
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Pricer needs a single command line argument");
        Main.main(input);
    }
    @Test
    public void invalidInputTest() throws Exception {
        String[] input = {"word"};
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument word must be an integer.");
        Main.main(input);
    }
//
//    @Test
//    public void validInputFromFileTest() {
//        String[] input = {"200"};
//        Main.main(input);
//        ByteArrayInputStream inContent;
//        Assert.assertEquals(inContent.toString(), outContent.toString());
//    }
}
