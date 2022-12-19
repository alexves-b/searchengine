package searchengine.services;

import searchengine.controllers.DefaultController;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {



        String text = "Повторное появление леопарда в Осетии позволяет предположить, что\n" +
                "леопард постоянно обитает в некоторых районах Северного Кавказа.\n\n";


        long start = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool();
        try {
            //pool.invoke(new ParseUrl(DefaultController.siteToParse));
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        long end = System.currentTimeMillis() - start;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(end);

        System.out.printf("Найдено: %d ссылок, за время %d минут \n", MyUtils.noDoublesUrlList.size(),minutes);



    }
}
