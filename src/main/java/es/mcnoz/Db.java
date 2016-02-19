package es.mcnoz.pereirachan;

import java.util.Collection;
import java.io.IOException;

public interface Db
{
    Collection<Post> getAllPosts() throws IOException;
    void writeAllPosts(Collection<Post> posts) throws IOException;
}

