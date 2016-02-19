package es.mcnoz.pereirachan;

import java.util.SortedSet;
import java.util.Collection;
import java.util.Date;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;

import java.nio.file.Path;
import java.nio.file.Files;

class CsvDb implements Db {
    private java.nio.file.Path mSavePath;

    CsvDb(String name,String dir) throws IOException {
        Path saveDir = java.nio.file.Paths.get(dir);
        if (!Files.exists(saveDir)) {
            Files.createDirectory(saveDir);
        }

        String filename = name + ".csv";
        mSavePath = saveDir.resolve(java.nio.file.Paths.get(filename));
        if (!Files.exists(mSavePath)) {
            Files.createFile(mSavePath);
        }
    }

    public boolean exists() {
        return java.nio.file.Files.exists(mSavePath);
    }

    // Get all posts from the database
    public Collection<Post> getAllPosts() throws IOException
    {
        System.out.println("reading all posts");
        BufferedReader in = null;
        try {
            Collection<Post> posts = new java.util.ArrayList<Post>();

            in = java.nio.file.Files.newBufferedReader(mSavePath);
            String line;
            while ((line = in.readLine()) != null) {
                Post post = fromCSV(line);
                posts.add(post);
            }

            return posts;

        } catch (IOException e) {
            throw e;

        } finally {
            try {
                if (in != null) in.close();
            }
            catch (IOException e) {
                System.err.println("Db: Error closing file");
            }
        }
    }

    // Put new posts in the database
    public void writeAllPosts(Collection<Post> posts)
        throws IOException
    {
        System.out.println("writting all posts");
        if (!exists()) {
            System.out.println("DB does not exist.");
            return;
        }

        Writer out = null;
        try {
            out = java.nio.file.Files.newBufferedWriter(mSavePath);
            for (Post p : posts) {
                out.write(toCSV(p));
            }
        } finally {
            if (out != null) out.close();
        }
    }

    private static String toCSV(Post p)
    {
        StringBuilder b = new StringBuilder();
        for (String f : p.getFields()) {
            b.append(f);
            b.append(',');
        }
        b.setCharAt(b.length()-1,'\n');
        return b.toString();
    }

    private static Post fromCSV(String line)
    {
        String[] f = line.split(",",8);
        Date date;
        try {
            date = Post.DATE_FORMAT.parse(f[2]);
        }
        catch (ParseException e) {
            date = null;
            System.err.println(e.getMessage());
        }
        return new Post(
                Integer.valueOf(f[0]),
                Integer.valueOf(f[1]),
                date,
                f[3],
                f[4],
                f[5],
                f[6],
                f[7]
                );
    }
}

