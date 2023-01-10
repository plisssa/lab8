import java.util.LinkedList;
public class URLPool {
    //создаем два списка для необработанных и обработанных ссылок
    private final LinkedList<URLDepthPair> openedLinks = new LinkedList<>();
    private final LinkedList<URLDepthPair> closedLinks = new LinkedList<>();
    //переменная хранит количество ожидающих потоков
    private int numWaiters = 0;

    public synchronized URLDepthPair getPair(){//метод заставляет поток ждать пока ссылок нет если появляется он ее получает
        while(openedLinks.size() == 0){
            numWaiters++;
            try{
                wait(); // заставляют поток ждать
            } catch (InterruptedException e) {
            }
            numWaiters--;
        }
        return openedLinks.removeFirst();
    }

    public synchronized void push(URLDepthPair pair){//метод пытается добавить ссылку в список необработанных если ее нет то добавит
        if (!openedLinks.contains(pair)) openedLinks.add(pair); //проверяем, есть ли такая ссылка, если нет, то добавляем
        notify(); // выводит поток из ожидания
    }

    public void pushClosed(URLDepthPair pair){
        if (!closedLinks.contains(pair)) closedLinks.add(pair);
    }//тоже самое только для обработанных ссылок
    //синхронизация не нужна поэтому без notify();
    //геттеры для ClosedLinks и NumWaiters
    public LinkedList<URLDepthPair> getClosedLinks(){
        return closedLinks;
    }
    public int getNumWaiters() {
        return numWaiters;
    }
}
