import java.util.LinkedList;
import java.net.*;
import java.io.*;
//класс для сканирования в нескольких потоках
//!
//класс наследует интерфейс ранэйбл
//программа понимает что на основе этого класса можем создать поток
public class CrawlerTask implements Runnable{
    //константы
    private static final String URL_PREFIX = "http://";
    private static final String URL_HREF = "<a href=";
    //храним ссылку на пул основной (из главного потока)
    private final URLPool pool;
    //храним максимальную глубину чтобы ее проверять
    private int maxDepth;
    public CrawlerTask(URLPool pool, int maxDepth){ //конструктор класса
        this.pool = pool;
        this.maxDepth = maxDepth;
    }
//1 метод read
    private LinkedList<URLDepthPair> read(URLDepthPair pair){
        //создаем сокет, подключаем через порт 80 устанавливаем для него таймаут, создаем входной и выходной потоки
        //отлавливаем ошибки
        //отправляем запрос на сервер, получаем ответ
        Socket socket;
        try{
            socket = new Socket(pair.getUrl().getHost(), 80);//создаем сокеты
        } catch (UnknownHostException e) {
            System.out.println("Ошибка: неизвестный хост: " + pair.getUrl().getHost());
            return null;
        } catch (IOException e) {
            System.out.println("Ошибка: ввода вывода при создании сокета: " + pair.getUrl().getHost());
            return null;
        }
        try{
            socket.setSoTimeout(1000);
        } catch (SocketException e) {
            System.out.println("Ошибка: установки таймаута сокета: " + pair.getUrl().getHost());
            return null;
        }
        BufferedReader in;
        PrintWriter out;
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //поток на вход чтобы считывать данные  с сервера
            out = new PrintWriter(socket.getOutputStream(),true); // на выход чтобы обратиться к серверу
        } catch (IOException e) {
            System.out.println("Ошибка: создания потоков ввода вывода: " + pair.getUrl().getHost());
            return null;
        }
        out.println("GET " + pair.getUrl().getPath()+" HTTP/1.1"); //строка на запрос как будет выводиться строка
        out.println("Host: " + pair.getUrl().getHost());
        out.println("Connection: close");
        out.println();
        LinkedList<URLDepthPair> links = new LinkedList<>(); //массив для ссылок
        String line;
        try {//построчный ответ получили и обрабатываем(при усл)
            //потом пытаемся создать новую url пару, если получилось то ссылка корректная и мы ее добавляем в список ссылок
            //если нет то перестаем обрабатывать и идем дальше
            //в итоге метод вернет список обработанных ссылок
            //идем по странице и добавляем в список ссылки
            while ((line = in.readLine()) != null){
                //построчно получаем Html документ
                if (!line.contains(URL_HREF)) continue;
                int startIndex = line.indexOf(URL_PREFIX);
                int endIndex = line.indexOf("\"", startIndex);
                if (startIndex == -1 || endIndex == -1) continue;
                URLDepthPair newPair;
                try{
                    newPair = new URLDepthPair(line.substring(startIndex,endIndex), pair.getDepth() + 1);
                } catch (MalformedURLException e) {
                    continue;
                }
                links.add(newPair);
            }
        } catch (IOException e) {
            System.out.println("Ошибка: получения Html документа: " + pair.getUrl().getHost());
            return null;
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Ошибка: закрытия сокета: " + pair.getUrl().getHost());
            return null;
        }
        return links;

    }
    @Override
    public void run() {//работа потока основной метод
        //поток получает ссылку обрабатывает ее добавляет новые ссылки в массив убирает старую ссылку из списка обработанных
        //так пока ссылки не закончатся
        while (true) {
            URLDepthPair pair = pool.getPair(); //получили из списка необработанных
            if (pair.getDepth() > maxDepth) continue;
            LinkedList<URLDepthPair> links = read(pair);
            if (links == null) continue;
            for (URLDepthPair elem: links){
                pool.push(elem);
            }
            pool.pushClosed(pair);
        }


    }
}
