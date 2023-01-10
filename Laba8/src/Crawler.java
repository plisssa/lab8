import java.net.MalformedURLException;
//основной файл, точка входа в программу
//сокет используется для установки соединения с веб сервером
public class Crawler {

    public static final String ERROR = "usage: java Crawler <URL><depth><numThreads>";
    public static void main(String[] args){//на вход приходит массив длиной 3
        if(args.length != 3){//проверка на длину
            System.out.println(ERROR);
            return;
        }
        //массив где первый аргумент ссылка (url) второй максимальная глубина (число) третий количество потоков
        int numThreads;
        int maxDepth;
        try{//переделываем из строк в числа
            maxDepth = Integer.parseInt(args[1]);
            numThreads = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {//возвращаем ошибку
            System.out.println(ERROR);//если буквы или что то еще
            return;
        }
        URLDepthPair firstPair;
        try{
            firstPair = new URLDepthPair(args[0], 0);//обработка ссылки, добавляем в класс кот хранит ссылку и глубину
        } catch (MalformedURLException e) {
            System.out.println(ERROR);//возвращает ошибку если с сылкой что-то не так
            return;
        }
        //сверху мы обработали все входные аргументы
        //Thread-многопоточность
        //мы создаем массив который хранит ссылки на обьекты которые управляют потоками
        //потом мы добавляем туда наши потоки
        //и запускаем их
        //хранит список адресов
        //URLPool опред-т какие адреса попадают в список необработан-х исходя из глубины каждого адреса добавляемого в пул
        URLPool pool = new URLPool();
        pool.push(firstPair);
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++){
            CrawlerTask task = new CrawlerTask(pool, maxDepth);
            threads[i] = new Thread(task);
            threads[i].start(); //запустили потоки
        }//основной поток ждет пока все другие потоки завершат работу завершает обработку и выводит результат
        while (pool.getNumWaiters() != numThreads) {
            try {
                Thread.sleep(500);//время ожидания
            } catch (InterruptedException e) {
            }
        }
        for (int i=0; i < numThreads; i++) {
            threads[i].stop();
        }
        System.out.println("Обработка завершена");
        for (URLDepthPair pair: pool.getClosedLinks()) {
            System.out.println(pair);
        }
    }
}
