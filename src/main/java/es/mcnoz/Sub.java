package es.mcnoz.pereirachan;

import java.net.URL;

import java.util.*;
import java.io.*;
import java.nio.file.Path;

/*
 * TODO:
 * - Add option to read from html file in disk.
 * - Create an interface for the type of website being processed, so that it
 *   can be given as input and kept decoupled.
 * - Implement "print unread posts".
 * - Fix up wait time.
 * - Fix newlines.
 * - Add methods for manipulating posts (eg, by id) externally.
 *
 * Problems:
 * - Will miss some saged posts if they're not in the first page.
 */

interface SubSource {
    Collection<Post> fetchNewPosts(Collection<Post> oldPosts)
        throws IOException;
    String getUrl();
}

class Sub implements Comparable<Sub>
{
    private SubSource mSource;
    private String mId;
    private Db mDb;
    private Double mWeight = new Double(1.0);
    private long mVisitWait = 800;  // milli

    private SortedSet<Post> mPosts = new TreeSet<>();

    static class SubBuilder {
        private SubSource mSource = null;
        private String mId = null;
        private Db mDb = null;
        private Double mWeight = new Double(1.0);

        SubBuilder(SubSource source) {
            mSource = source;
        }

        public SubBuilder setWeight(Double weight)
        { mWeight = weight; return this; };

        public SubBuilder setId(String id)
        { mId = id; return this; };

        public SubBuilder setDb(Db db)
        { mDb = db; return this;};

        Sub getInstance() throws IOException {
            Sub sub = new Sub();
            sub.mSource = this.mSource;
            sub.mId = this.mId;
            sub.mDb = this.mDb;
            if (mDb != null) {
                for (Post p : mDb.getAllPosts()) {
                    sub.addPost(p);
                }
            }
            return sub;
        }
    }

    // ---------------

    /* Visit board page. Get new posts. */
    void update() throws IOException
    {
        System.out.println("Updating " + toString());
        Collection<Post> newPosts = mSource.fetchNewPosts(mPosts);
        for (Post p : newPosts) {
            addPost(p);
        }
        if (mDb != null) {
            mDb.writeAllPosts(newPosts);
        }
    }

    private void addPost(Post p) {
        mPosts.add(p);

        // logging
        String pStr = p + "\n";
        if (!mId.isEmpty())
            pStr = mId + "/" + pStr;
        if (p.isTopic()) 
            pStr = "-> " + pStr;
        System.out.println(pStr);
    }

    private boolean exists(Post post) {
        return mPosts.contains(post);
    }

    public void print() {
        for (Post p : mPosts)
            System.out.println(p);
    }

    public void print(int n) {
        for (Post p : latest(n))
            System.out.println(p);
    }

    // get latest posts
    public List<Post> latest(int n) {
        List<Post> l = new ArrayList<>();
        int i = 0;
        for (Post p : mPosts) {
            l.add(p);
            if (i++ == n)
                break;
        }
        return l;
    }

    public void statistics() {
        System.out.println("No. posts: " + mPosts.size());
    }

    public String toString() {
        return '[' + mId + " - " + mSource.getUrl() + ']';
    }

    public Double getWeight() {
        return mWeight;
    }

    public int compareTo(Sub o) {
        return mWeight.compareTo(o.mWeight);
    }

    public boolean equals(Object o)
    {
        if (o instanceof Sub) {
            return mSource.getUrl().equals(((Sub)o).mSource.getUrl());
        }
        return false;
    }

    // Main -------

    public static void main(String[] args)
    {
        String uri = args[0];
        try {
            Sub sub = new SubBuilder(new PtchanSource(args[0])).getInstance();
            sub.update();
            //sub.print(5);
        }
        catch (IOException e) {
            //System.err.println(e.getMessage());
            //e.printStackTrace();
            System.err.println(e);
        }
    }
}


