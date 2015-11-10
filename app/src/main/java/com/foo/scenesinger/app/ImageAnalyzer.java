package com.foo.scenesinger.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by sibigtroth on 7/14/14.
 */

public class ImageAnalyzer
{

    private BrainActivity brainActivity;
    private ArrayList<ArrayList<Point>> representativeSubClusters;
    private Random random;

    public ImageAnalyzer(BrainActivity brainActivity)
    {
        this.brainActivity = brainActivity;

        this.Init();
    }

    private void Init()
    {
        this.random = new Random();
    }


    ///////////////////////////
    //accessors
    ///////////////////////////

    private BrainActivity GetBrainActivity() {return this.brainActivity;}


    ///////////////////////////
    //callbacks
    ///////////////////////////


    ///////////////////////////
    //utilities
    ///////////////////////////

    public Point FindRepresentativeClusterPointForGivenClusterIndex(int index_cluster)
    {
        Point point = null;

        ArrayList<Point> representativeSubCluster = this.representativeSubClusters.get(index_cluster);
        if (representativeSubCluster.size() > 0)
        {
            int randIndex = this.random.nextInt(representativeSubCluster.size());
            point = representativeSubCluster.get(randIndex);

            //Log.i("foo", "FindRepresentativePixelCoordForGivenClusterIndex " + point.x + " " + point.y + " " + point.z);
        }

        return point;
    }






    /*
    public void FindImagePalette(String filePath_image)
    {
        int numPoints = 100000;
        Point[] points = new Point[numPoints];
        Random random = new Random();
        for (int i=0; i<numPoints; i++)
        {
            int x_rand = random.nextInt(256);
            int y_rand = random.nextInt(256);
            int z_rand = random.nextInt(256);
            Point point = new Point(x_rand, y_rand, z_rand);
            points[i] = point;
        }

        int numClusters = 5;
        int width = 255;
        int height = 255;
        int depth = 255;

        DalvikClusterer dalvikClusterer = new DalvikClusterer();
        dalvikClusterer.cluster(points, numClusters, width, height, depth);
    }
    */

    public ArrayList<Integer> FindImagePalette(String filePath_image)
    {
        float scale_new = .1f;
        Bitmap bitmap_resized = this.ResizeImage(filePath_image, scale_new);
        BitmapPixel[] bitmapPixels = this.CollectBitmapPixels(bitmap_resized);
        int numPixels = bitmapPixels.length;
        Point[] points = new Point[numPixels];
        for (int i=0; i<numPixels; i++)
        {
            int pixelColor = bitmapPixels[i].color;
            int r = Color.red(pixelColor);
            int g = Color.green(pixelColor);
            int b = Color.blue(pixelColor);
            //make sure to scale back up the pixel coords (since we scaled down the original image when collecting pixels)
            float x_fullSizeImage = ((float)bitmapPixels[i].x / scale_new);
            float y_fullSizeImage = ((float)bitmapPixels[i].y / scale_new);
            //now map these values to the screen size
            //cheat with hardcoded values
            //screensize 640, 360
            //taken picture size 1024 576
            float scaleFactor = 640f/1024f;
            int x = (int)(x_fullSizeImage * scaleFactor);
            int y = (int)(y_fullSizeImage * scaleFactor);
            //Log.i("foo", "FindPaletteImage   x " + x + "  y  " + y);
            Point point = new Point(r, g, b, x, y);
            points[i] = point;
        }

        int numClusters = 5;
        int width = 255;
        int height = 255;
        int depth = 255;

        DalvikClusterer dalvikClusterer = new DalvikClusterer();
        Point[] colors_rgb = dalvikClusterer.cluster(points, numClusters, width, height, depth);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int j=0; j<colors_rgb.length; j++)
        {
            int r = colors_rgb[j].x;
            int g = colors_rgb[j].y;
            int b = colors_rgb[j].z;
            //Log.i("foo", "r:  " + r + "  g:  " + g + "  b:  " + b);
            int color = Color.rgb(r, g, b);
            //Log.i("foo", "color:  " + color);
            colors.add(color);
        }

        this.StoreRepresentativeSubClusters(points, dalvikClusterer.means, dalvikClusterer);

        return colors;
    }


    public ArrayList<Integer> FindImagePalette(Bitmap bitmap)
    {
        float scale_new = .1f;
        Bitmap bitmap_resized = this.ResizeBitmap(bitmap, scale_new);
        BitmapPixel[] bitmapPixels = this.CollectBitmapPixels(bitmap_resized);
        int numPixels = bitmapPixels.length;
        Point[] points = new Point[numPixels];
        for (int i=0; i<numPixels; i++)
        {
            int pixelColor = bitmapPixels[i].color;
            int r = Color.red(pixelColor);
            int g = Color.green(pixelColor);
            int b = Color.blue(pixelColor);
            //make sure to scale back up the pixel coords (since we scaled down the original image when collecting pixels)
            float x_fullSizeImage = ((float)bitmapPixels[i].x / scale_new);
            float y_fullSizeImage = ((float)bitmapPixels[i].y / scale_new);
            //now map these values to the screen size
            //cheat with hardcoded values
            //screensize 640, 360
            //taken picture size 1024 576
            float scaleFactor = 640f/1024f;
            int x = (int)(x_fullSizeImage * scaleFactor);
            int y = (int)(y_fullSizeImage * scaleFactor);
            //Log.i("foo", "FindPaletteImage   x " + x + "  y  " + y);
            Point point = new Point(r, g, b, x, y);
            points[i] = point;
        }

        int numClusters = 5;
        int width = 255;
        int height = 255;
        int depth = 255;

        DalvikClusterer dalvikClusterer = new DalvikClusterer();
        Point[] colors_rgb = dalvikClusterer.cluster(points, numClusters, width, height, depth);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int j=0; j<colors_rgb.length; j++)
        {
            int r = colors_rgb[j].x;
            int g = colors_rgb[j].y;
            int b = colors_rgb[j].z;
            //Log.i("foo", "r:  " + r + "  g:  " + g + "  b:  " + b);
            int color = Color.rgb(r, g, b);
            //Log.i("foo", "color:  " + color);
            colors.add(color);
        }

        this.StoreRepresentativeSubClusters(points, dalvikClusterer.means, dalvikClusterer);

        return colors;
    }


    private void StoreRepresentativeSubClusters(Point[] points, Point[] means, DalvikClusterer dalvikClusterer)
    {
        this.representativeSubClusters = new ArrayList<ArrayList<Point>>();
        for (int j=0; j<means.length; j++)
        {
            ArrayList<Point> representativeSubCluster = new ArrayList<Point>();
            this.representativeSubClusters.add(representativeSubCluster);
        }

        double distanceThreshold = 25;//10;

        //loop through all the points
        for (int i=0; i<points.length; i++)
        {
            //get the distance between this point and its cluster mean
            int clusterForThisPoint = points[i].cluster;
            Point meanForThisCluster = means[clusterForThisPoint];
            double distanceBetweenThisPointAndClusterMean = dalvikClusterer.computeDistance(points[i], meanForThisCluster);

            //if this distance is small enough
            if (distanceBetweenThisPointAndClusterMean <= distanceThreshold)
            {
                //add it to the stored representative cluster
                this.representativeSubClusters.get(clusterForThisPoint).add(points[i]);
            }
        }

        for (int k=0; k<this.representativeSubClusters.size(); k++)
        {
            Log.i("foo", "-----------  rep sub cluster size  ----------" + this.representativeSubClusters.get(k).size());
        }

    }

    public Bitmap ResizeImage(String filePath_image, float scale_new)
    {
        Bitmap bitmap_orig = BitmapFactory.decodeFile(filePath_image);
        int w_bitmap_orig = bitmap_orig.getWidth();
        int h_bitmap_orig = bitmap_orig.getHeight();
        //Log.i("foo", "w: " + w_bitmap_orig + "  h:   " + h_bitmap_orig);
        int w_bitmap_new = (int)(w_bitmap_orig * scale_new);
        int h_bitmap_new = (int)(h_bitmap_orig * scale_new);
        Bitmap bitmap_resized;
        bitmap_resized = Bitmap.createScaledBitmap(bitmap_orig, w_bitmap_new, h_bitmap_new, false);

        return bitmap_resized;
    }

    public Bitmap ResizeBitmap(Bitmap bitmap, float scale_new)
    {
        Bitmap bitmap_orig = bitmap;
        int w_bitmap_orig = bitmap_orig.getWidth();
        int h_bitmap_orig = bitmap_orig.getHeight();

        int w_bitmap_new = (int)(w_bitmap_orig * scale_new);
        int h_bitmap_new = (int)(h_bitmap_orig * scale_new);
        Bitmap bitmap_resized;
        bitmap_resized = Bitmap.createScaledBitmap(bitmap_orig, w_bitmap_new, h_bitmap_new, false);

        return bitmap_resized;
    }

    private BitmapPixel[] CollectBitmapPixels(Bitmap bitmap)
    {
        int w_bitmap = bitmap.getWidth();
        int h_bitmap = bitmap.getHeight();
        int numPixels = w_bitmap * h_bitmap;
        BitmapPixel[] bitmapPixels = new BitmapPixel[numPixels];

        int pixelCounter = 0;
        for (int x=0; x<w_bitmap; x++)
        {
            for (int y = 0; y < h_bitmap; y++)
            {
                int color = bitmap.getPixel(x, y);
                bitmapPixels[pixelCounter] = new BitmapPixel(color, x, y);
                pixelCounter += 1;
            }
        }

        return bitmapPixels;
    }

    class BitmapPixel
    {
        public int color;
        public int x;
        public int y;

        public BitmapPixel(int color, int x, int y)
        {
            this.color = color;
            this.x = x;
            this.y = y;
        }
    }








    //edited code based on that found at:
    //https://code.google.com/p/hdict/source/browse/src/com/google/io/kmeans/?r=66e5aa096d9b323ac685a41165aa668d90819df5


    public class DalvikClusterer
    {
        private static final int MAX_LOOP_COUNT = 5;//15;
        private double[] distances;
        private final Random random = new Random(System.currentTimeMillis());
        public Point[] means;

        public Point[] cluster(Point[] points, int numClusters, int width, int height, int depth)
        {
            Log.d("foo", "start clustering");

            boolean converged = false;
            boolean dirty;
            double distance;
            double curMinDistance;
            int loopCount = 0;
            Point point;
            distances = new double[points.length];

            // randomly pick some points to be the centroids of the groups, for the first pass
            this.means = new Point[numClusters];
            for (int i = 0; i < numClusters; ++i) {
                //means[i] = new Point(random.nextInt(width), random.nextInt(height), random.nextInt(depth));
                int index_randPoint = this.random.nextInt(points.length);
                Point point_rand = points[index_randPoint];
                means[i] = new Point(point_rand.x, point_rand.y, point_rand.z, -1, -1);
                means[i].cluster = i;
            }

            // initialize data
            for (int i = 0; i < points.length; ++i) {
                distances[i] = Double.MAX_VALUE;
            }
            int[] sumX = new int[numClusters];
            int[] sumY = new int[numClusters];
            int[] sumZ = new int[numClusters];
            int[] clusterSizes = new int[numClusters];

            // main loop
            while (!converged) {
                //Log.i("foo", "a");
                dirty = false;
                // compute which group each point is closest to
                for (int i = 0; i < points.length; ++i) {
                    point = points[i];
                    curMinDistance = distances[i];
                    for (Point mean : means) {
                        distance = computeDistance(point, mean);
                        if (distance < curMinDistance) {
                            dirty = true;
                            distances[i] = distance;
                            curMinDistance = distance;
                            point.cluster = mean.cluster;
                        }
                    }
                }

                // if we did no work, break early (greedy algorithm has converged)
                if (!dirty) {
                    converged = true;
                    break;
                }

                // compute the new centroids of the groups, since contents have changed
                for (int i = 0; i < numClusters; ++i) {
                    sumX[i] = sumY[i] = sumZ[i] = clusterSizes[i] = 0;
                }

                for (int i = 0; i < points.length; ++i) {
                    point = points[i];
                    sumX[point.cluster] += point.x;
                    sumY[point.cluster] += point.y;
                    sumZ[point.cluster] += point.z;
                    clusterSizes[point.cluster] += 1;
                }

                for (int i = 0; i < numClusters; ++i) {
                    //Log.i("foo", "cluster size for  " + i + ":  " + clusterSizes[i]);
                }

                for (int i = 0; i < numClusters; ++i) {
                    //Log.d("foo", "loop count internal:  " + String.valueOf(loopCount) + "    i:  " + String.valueOf(i));

                    //matt added this if statement to account for bigger numClusters
                    //for big numClusters, it's possible that some clusters don't have any points associated
                    //so their clusterSize is zero, which leads to the division by zero below
                    if (clusterSizes[i] != 0) {
                        means[i].x = (int) (sumX[i] / clusterSizes[i]);
                        means[i].y = (int) (sumY[i] / clusterSizes[i]);
                        means[i].z = (int) (sumZ[i] / clusterSizes[i]);
                    }
                }

                //Log.i("foo", "current loop count:  " + loopCount);

                // bail out after at most MAX_LOOP_COUNT passes
                loopCount++;


                converged = converged || (loopCount > MAX_LOOP_COUNT);
            }
            //Log.d("foo", "first cluster:  " + "x "  + String.valueOf(means[0].x) + "    y " + String.valueOf(means[0].y) + "    z " + String.valueOf(means[0].z));

            for (int index_mean=0; index_mean<means.length; index_mean++)
            {
                //Log.i("foo", "cluster " + String.valueOf(index_mean) + "   x "  + String.valueOf(means[index_mean].x) + "    y " + String.valueOf(means[index_mean].y) + "    z " + String.valueOf(means[index_mean].z));
            }

            for (int i = 0; i < numClusters; ++i) {
                if (clusterSizes[i] == 0)
                {
                    //Log.i("foo", "setting cluster to null <<<<<<<<<<<<<<<<<");
                    means[i].x = 0;
                    means[i].y = 0;
                    means[i].z = 0;
                }
            }

            //Log.i("foo", "loop count:   " + String.valueOf(loopCount));
            //Log.i("foo", "done clustering");



            return means;
        }


        //Computes the Cartesian distance between two points.
        private double computeDistance(Point a, Point b) {
            return Math.sqrt( (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) + (a.z - b.z) * (a.z - b.z) );
        }
    }

    public static class Point
    {
        public int x;
        public int y;
        public int z;
        public int cluster;
        public int x_pixel;
        public int y_pixel;

        public Point(int d, int e, int f, int x_pixel, int y_pixel)
        {
            this.x = d;
            this.y = e;
            this.z = f;
            this.x_pixel = x_pixel;
            this.y_pixel = y_pixel;
        }
    }



}
