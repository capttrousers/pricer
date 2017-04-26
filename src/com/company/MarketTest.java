package com.company;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


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
    public void processesFirstTenLinesOfInputFileAt200() {
        Book marketBook = new Book();
        Main.updateBook("28800538 A b S 44.26 100", marketBook);
        Main.updateBook("28800562 A c B 44.10 100", marketBook);
        Main.updateBook("28800744 R b 100", marketBook);
        Main.updateBook("28800758 A d B 44.18 157", marketBook);
        Main.updateBook("28800773 A e S 44.38 100", marketBook);
        Main.updateBook("28800796 R d 157", marketBook);
        Main.updateBook("28800812 A f B 44.18 157", marketBook);
        String value = marketBook.completeOrder("sell", 200);
        Assert.assertEquals("8832.56", value);
    }

    @Test
    public void processesFirst65LinesOfInputFileAtTargetSize1() {
        Book marketBook = new Book();
        Main.updateBook("28800538 A b S 44.26 100", marketBook);
        Assert.assertEquals("44.26", marketBook.completeOrder("buy", 1));
        Main.updateBook("28800562 A c B 44.10 100", marketBook);
        Main.updateBook("28800744 R b 100", marketBook);
        Main.updateBook("28800758 A d B 44.18 157", marketBook);
        Main.updateBook("28800773 A e S 44.38 100", marketBook);
        Main.updateBook("28800796 R d 157", marketBook);
        Main.updateBook("28800812 A f B 44.18 157", marketBook);
        Main.updateBook("28800974 A g S 44.27 100", marketBook);
        Main.updateBook("28800975 R e 100", marketBook);
        Main.updateBook("28812071 R f 157", marketBook);
        Main.updateBook("28813129 A h B 43.68 50", marketBook);
        Main.updateBook("28813830 A i S 44.18 100", marketBook);
        Main.updateBook("28814087 A j S 44.18 1000", marketBook);
        Main.updateBook("28814834 R c 100", marketBook);
        Main.updateBook("28814864 A k B 44.09 100", marketBook);
        Main.updateBook("28815774 R k 100", marketBook);
        Main.updateBook("28815804 A l B 44.07 100", marketBook);
        Main.updateBook("28815937 R j 1000", marketBook);
        Main.updateBook("28816244 R l 100", marketBook);
        Main.updateBook("28816245 A m S 44.22 100", marketBook);
        Main.updateBook("28816245 R g 100", marketBook);
        Main.updateBook("28816273 A n B 44.03 100", marketBook);
        Main.updateBook("28817570 A o S 44.14 170", marketBook);
        Main.updateBook("28822172 R o 20", marketBook);
        Main.updateBook("28823984 A p B 44.04 100", marketBook);
        Main.updateBook("28823984 R n 100", marketBook);
        Main.updateBook("28824454 R p 100", marketBook);
        Main.updateBook("28824484 A q B 44.03 100", marketBook);
        Main.updateBook("28826314 R q 100", marketBook);
        Main.updateBook("28826343 A r B 43.89 100", marketBook);
        Main.updateBook("28826384 R r 100", marketBook);
        Main.updateBook("28826414 A s B 43.78 100", marketBook);
        Main.updateBook("28826424 R s 100", marketBook);
        Main.updateBook("28826454 A t B 43.75 100", marketBook);
        Main.updateBook("28826455 R t 100", marketBook);
        Main.updateBook("28826485 A u B 43.72 100", marketBook);
        Main.updateBook("28835564 A v B 43.85 100", marketBook);
        Main.updateBook("28835565 R u 100", marketBook);
        Main.updateBook("28838797 A w S 44.15 500", marketBook);
        Main.updateBook("28841307 A x S 44.40 100", marketBook);
        Main.updateBook("28845097 A y S 44.10 500", marketBook);
        Main.updateBook("28845547 R i 100", marketBook);
        Main.updateBook("28845606 A z S 44.13 100", marketBook);
        Main.updateBook("28854850 R w 500", marketBook);
        Main.updateBook("28855165 A ab B 43.91 100", marketBook);
        Main.updateBook("28855165 R v 100", marketBook);
        Main.updateBook("28860037 A bb S 44.16 2000", marketBook);
        Main.updateBook("28861264 A cb S 44.17 100", marketBook);
        Main.updateBook("28861264 R m 100", marketBook);
        Main.updateBook("28864794 A db S 44.11 100", marketBook);
        Main.updateBook("28864794 R cb 100", marketBook);
        Main.updateBook("28864870 A eb S 44.14 500", marketBook);
        Main.updateBook("28865273 A fb S 44.10 100", marketBook);
        Main.updateBook("28867484 R db 100", marketBook);
        Main.updateBook("28867513 A gb S 44.17 100", marketBook);
        Main.updateBook("28869845 A hb B 43.92 100", marketBook);
        Main.updateBook("28873954 A ib S 44.11 100", marketBook);
        Main.updateBook("28873954 R gb 100", marketBook);
        Main.updateBook("28874354 R ab 100", marketBook);
        Main.updateBook("28874384 A jb B 43.78 100", marketBook);
        Main.updateBook("28875094 A kb S 44.11 100", marketBook);
        Main.updateBook("28875094 R ib 100", marketBook);
        Main.updateBook("28875613 R fb 100", marketBook);
        Main.updateBook("28876376 R hb 100", marketBook);
        Main.updateBook("28876444 R jb 100", marketBook);

        String value = marketBook.completeOrder("sell", 1);
//        Assert.assertEquals("44.56", value);
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
