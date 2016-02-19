package es.mcnoz.pereirachan;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeSet;
import java.util.SortedSet;
import java.io.IOException;

/* TODO:
 * - Register single topics (make ChanTopic type);
 * - Add support for other imageboards (manelchan); bulletin boards; etc.
 * - Read from config file (JSON?)
 * - Add weights.
 *
 * - Each board/dumper should be run in a thread.
 * - Extract images; create subdirectories for images.
 * - Dump into database.
 * - Country flags.
 */
public class Website implements Runnable
{
    private List<Sub> mSubs = new ArrayList<>();
    private double totalWeight = 0.0;
    private Random mRandom = new Random(System.currentTimeMillis());
    protected Db mDb;
    protected long mInterSubWait;  // msec

    protected Website(long waitPeriod) {
        mInterSubWait = waitPeriod;
    }
    // ----------------------------

    public void run() {
        Collections.sort(mSubs);

        int numRandomUpdatesInARow = 500;
        int maxNumRetries = 3;

        System.out.println("Number of subsites: " + mSubs.size());
        for (Sub b : mSubs) {
            System.out.println(b);
        }

        try {

        while (true) {
            // Update all subs
            for (Sub b : mSubs) {
                System.out.println("updating");
                updateSub(b,maxNumRetries);
            }

            // Update subs at random according to weights
            for (int i=0; i < numRandomUpdatesInARow; ++i) {
                System.out.println("random");
                updateSub(getRandomSub(),maxNumRetries);
            }
        }

        } catch (Exception e) {
            System.out.println("asdasd 2");
             e.printStackTrace();
        }
        System.out.println("asdsdasd");
    }

    private void updateSub(Sub b,int retries) throws IOException {
        // wait for a while
        try { Thread.sleep(mInterSubWait); }
        catch (InterruptedException e) { }

        // try to update a number of times
        while (true) {
            try {
                b.update();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                if (retries-- == 0) {
                    throw e;
                } else {
                    continue;
                }
            }
            break;
        }
    }

    // TODO: check that this is bug free
    private Sub getRandomSub() {
        if (mSubs.isEmpty()) { return null; }

        final Double val = mRandom.nextDouble()*totalWeight;
        System.out.println("debug: val: " + val);

        int i = 0;
        double sum = 0.0;
        for (Sub b : mSubs) {
            double weight = b.getWeight();
            if (val >= sum && val <= sum + weight) {
                //System.out.println("debug: random number: " + i);
                return b;
            }
            sum += weight;
            ++i;
        }
        //System.out.println("debug: random number: " + i);
        return mSubs.get(mSubs.size()-1);
    }

    // add sub with weight
    protected void register(Sub sub)
        throws IOException
    {
        mSubs.add(sub);
        totalWeight += sub.getWeight();
    }

    // return sorted list with the lastest post from all subs
    public List<Post> latest(int n) {
        Queue<Post> queue = new PriorityQueue<>();
        for (Sub b : mSubs)
            queue.addAll(b.latest(n));
        List<Post> list = new ArrayList<>();
        while (n-- > 0)
            list.add(queue.poll());
        return list;
    }
}
