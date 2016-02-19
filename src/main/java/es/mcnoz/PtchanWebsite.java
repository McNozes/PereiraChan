package es.mcnoz.pereirachan;

import java.io.IOException;
import java.util.List;
import java.util.Deque;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

//import org.w3c.dom.Document;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PtchanWebsite extends Website
{
    public static final int mInterBoardWait = 800; // msec

    public PtchanWebsite() throws IOException {
        super(mInterBoardWait);

        PtchanSource.mInterTopicWait = 200;

        addBoard("http://ptchan.net/a",     "a"     , 1.0 );
        addBoard("http://ptchan.net/b",     "b"     , 15.0);
        addBoard("http://ptchan.net/con",   "con"   , 3.0 );
        addBoard("http://ptchan.net/c",     "c"     , 2.0 );
        addBoard("http://ptchan.net/cu",    "cu"    , 1.0 );
        addBoard("http://ptchan.net/des",   "des"   , 1.0 );
        addBoard("http://ptchan.net/dis",   "dis"   , 1.0 );
        addBoard("http://ptchan.net/fit",   "fit"   , 1.0 );
        addBoard("http://ptchan.net/o",     "o"     , 1.0 );
        addBoard("http://ptchan.net/t",     "t"     , 3.0 );
        addBoard("http://ptchan.net/u",     "u"     , 3.0 );
        addBoard("http://ptchan.net/xxx",   "xxx"   , 2.0 );
        addBoard("http://ptchan.net/int",   "int"   , 1.0 );
        addBoard("http://ptchan.net/pt",    "pt"    , 2.0 );
        addBoard("http://ptchan.net/meta",  "meta"  , 1.0 );
    }

    private void addBoard(String url,String id,double weight) 
        throws IOException
    {
        Sub.SubBuilder builder = new Sub.SubBuilder(new PtchanSource(url));

        Db db = new CsvDb(id,"csv");

        Sub sub = builder
            .setId(id)
            .setWeight(weight)
            .setDb(db)
            .getInstance();

        register(sub);
    }

    public static void main(String[] args) throws IOException {
        new PtchanWebsite().run();
    }
}

class PtchanSource implements SubSource
{
    private String mUrl = null;
    private int mNumPages = -1;

    public static long mInterTopicWait = 100; // msec

    PtchanSource(String url) {
        mUrl = url;
    }

    public String getUrl() { return mUrl; }

    public Collection<Post> fetchNewPosts(Collection<Post> oldPosts)
        throws IOException {
        if (mNumPages == -1) {      // lazy loading
            mNumPages = numberOfPages();
        }

        // get posts
        List<Post> newPosts = new java.util.LinkedList<>();
        try {
            for (int page=0; page < mNumPages; page++) {
                String url = page == 0 ? mUrl : pageURL(page);
                Document doc = Jsoup.connect(url).get();
                System.out.println("Fetched " + url);
                for (int id : getBoardDocTopicIds(doc)) {
                    Collection<Post> l = visitTopic(id,oldPosts);
                    if (page != 0 && l.isEmpty())
                        return newPosts;
                    newPosts.addAll(l);

                    try { Thread.sleep(mInterTopicWait); }
                    catch (InterruptedException e) { }
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error in " + mUrl);
            throw e;
        }
        return newPosts;
    }

    private int numberOfPages()
    {
        int lastPage = 0;
        try {
            Document doc = Jsoup.connect(mUrl).get();
            lastPage = Integer.valueOf(
                    doc
                    .select("td a")
                    .select("[href~=^/[a-z]+/\\d+.html$]")
                    .select(":matchesOwn(\\d+)")
                    .last()
                    .ownText());
        } catch (IOException e ) {
            e.getMessage();
        }
        return lastPage + 1;
    }


    /* Visit topic page. Get new posts. */
    private Collection<Post> visitTopic(int topicId,Collection<Post> oldPosts)
        throws IOException
    {
        //System.out.println("Visiting " + url);
        Deque<Post> newPosts = new java.util.LinkedList<Post>();
        try {
            Document doc = Jsoup.connect(topicURL(topicId)).get();
            Elements topicPosts = getTopicDocPosts(doc);

            // iterate backwards
            for (int i=topicPosts.size()-1; i >= 0; i--)
            {
                Element elem = topicPosts.get(i);
                Post p = parsePost(elem,topicId);
                if (oldPosts.contains(p))
                    break;
                newPosts.addFirst(p);
            }
        }
        catch (IOException e) {
            System.err.println("Error in " + e);
            throw e;
        }
        return newPosts;
    }

    /* Extract from DOM node. */
    private Post parsePost(Element elem,int topicId)
    {
        int postId = getPostId(elem);

        String postText = elem
            .getElementsByTag("blockquote")
            .first()
            .child(0)
            .text();  // not ownText because of links

        String userId = elem.ownText();;
        if (userId.matches("^ID: [a-f0-9]{6}"))
            userId = userId.replaceAll("^ID: ","");
        else
            userId = "";

        String date = elem
            .getElementsByTag("label")
            .first()
            .ownText()
            .replaceAll("^[A-Z][a-z]{2} ","");
        Date postDate;
        try {
            DateFormat df = new SimpleDateFormat("d/MMM/yyyy HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            postDate = df.parse(date);
        }
        catch (ParseException e) {
            System.err.println(e.getMessage());
            postDate = null;
        }

        String userName = elem
            .getElementsByClass("postername")
            .first()
            .text(); // not ownText because of links

        Element imgElem = elem.getElementsByClass("filesize").first();
        String imgName;
        String imgLink;
        if (imgElem == null) {
            imgName = "";
            imgLink = "";
        } else {
            imgName = (imgElem.ownText().split(","))[2]
                .replaceAll(" ","")
                .replaceAll("[)]$","");
            imgLink = imgElem.child(0).text();
        }

        return new Post(
                topicId,
                getPostId(elem),
                postDate,
                userName,
                userId,
                imgName,
                imgLink,
                postText
                );
    }

    // URLs

    private String pageURL(int page) {
        return mUrl + "/" + page + ".html";
    }

    private String topicURL(int id) {
        return mUrl + "/res/" + id + ".html";
    }

    private String imageURL(String name) {
        return mUrl + "/src/" + name;
    }

    // Board Document
    private Elements getBoardDocTopics(Document doc)
    {
        return doc.getElementsByAttributeValueMatching("id",
                "^thread\\d+[a-z]+$");
    }

    private Collection<Integer> getBoardDocTopicIds(Document doc)
    {
        Deque<Integer> l = new java.util.ArrayDeque<>();
        for (Element e : getBoardDocTopics(doc))
            l.addFirst(Integer.valueOf(e.id().replaceAll("[a-z]+","")));
        return l;
    }

    // Topic Document
    private Elements getTopicDocPosts(Document doc)
    {
        return doc.getElementsByAttributeValueMatching("id",
                "^(thread\\d+[a-z]+)|(reply\\d+)$");
    }

    // Post Element
    private int getPostId(Element e)
    {
        return Integer.valueOf(
                e
                .getElementsByAttributeValueMatching("name","\\d+").first()
                .attr("name"));
    }

    String getIdFromUrl() {
        // TODO
        return null;
    }
}
