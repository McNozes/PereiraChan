package es.mcnoz.pereirachan;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import java.io.*;


// TODO: use setters and getters
class Post implements Comparable<Post>
{
    public final Integer mTopicID;
    public final Integer mPostID;
    public final Date    mDate;
    public final String  mPosterName;
    public final String  mPosterID;
    public final String  mImgName;
    public final String  mImgLinkName;
    public final String  mText;

    public static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("d/MMM/yyyy HH:mm:ss");

    public Post(
            int topicID,
            int postID,
            Date date,
            String posterName,
            String posterID,
            String imgName,
            String imgLinkName,
            String text)
    {
        mTopicID = topicID;
        mPostID = postID;
        mDate = date;
        mPosterName = posterName;
        mPosterID = posterID;
        mImgName = imgName;
        mImgLinkName = imgLinkName;
        mText = text;
    }

    // ------------------

    public boolean isTopic() {
        return mTopicID.compareTo(mPostID) == 0;
    }

    /* Compare Date, and then compare post number. */
    public int compareTo(Post o)
    {
        int dcomp = (o.mDate).compareTo(this.mDate);
        if (dcomp != 0) return dcomp;
        return (o.mPostID).compareTo(mPostID);
    }

    public boolean equals(Object o)
    {
        if (o instanceof Post) {
            return (this.compareTo(((Post)o)) == 0);
        } else
            return false;
    }

    public String toString()
    {
        String s = mTopicID + "/" + mPostID + " " + DATE_FORMAT.format(mDate) +
            " " + mPosterName;
        if (!mPosterID.isEmpty())
            s += " " + mPosterID;
        if (!mImgName.isEmpty())
            s += " " + mImgName + " " + mImgLinkName;
        s += "\n" + mText;
        return s;
    }

    public List<String> getFields() {
        List<String> fields = new java.util.ArrayList<>();
        fields.add(mTopicID.toString());
        fields.add(mPostID.toString());
        fields.add(DATE_FORMAT.format(mDate));
        fields.add(mPosterName);
        fields.add(mPosterID);
        fields.add(mImgName);
        fields.add(mImgLinkName);
        fields.add(mText);
        return fields;
    }
}
