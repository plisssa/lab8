import java.net.*;
//класс хранит совместное url и хранит текущую глубину для этой ссылки
public class URLDepthPair {
    private URL url;
    private int depth;

    public URLDepthPair(String url, int depth) throws MalformedURLException{//создает объект url на основе строки
        //и сохраняет глубину (если с url что то не так то он выкинет при создании объекта
        this.url = new URL(url);
        this.depth = depth;
    }
    //геттер
    public URL getUrl(){
        return url;
    }
    //геттер
    public int getDepth() {
        return depth;
    }

    public String toString(){
        return url.toString() + " " + depth;
    }//метод позволяет выводить объект в консоль
    //сначала ссылка пробел потом глубина

    @Override
    public boolean equals(Object o){//метод позволяет сравнивать две ссылки (в urlpool в contains)
        if(o instanceof URLDepthPair p){
            return url.equals(p.url);
        }
        return false;
    }
    @Override
    public int hashCode(){
        return url.hashCode();
    }
}
