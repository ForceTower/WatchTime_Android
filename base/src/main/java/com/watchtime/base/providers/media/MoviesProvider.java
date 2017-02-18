package com.watchtime.base.providers.media;

import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.R;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.providers.media.models.Genre;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.providers.media.models.Movie;
import com.watchtime.base.providers.media.models.Person;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by João Paulo on 16/02/2017.
 */

public class MoviesProvider extends MediaProvider{
    private static final MoviesProvider mediaProvider = new MoviesProvider();
    private static Integer CURRENT_API = 0;
    private static final String[] API_URLS = ApiEndPoints.BASE_MOVIES_URLS;
    private static Filters filters = new Filters();

    @Override
    protected OkHttpClient getClient() {
        return super.getClient(); //TODO: Set up new client here
    }

    @Override
    protected Call enqueue(Request request, okhttp3.Callback requestCallback) {
        return super.enqueue(request, requestCallback);
    }

    @Override
    public Call getList(ArrayList<Media> currentList, Filters filter, Callback callback) {
        filters = filter;

        final ArrayList<Media> list;
        if (currentList == null) {
            list = new ArrayList<>();
        } else {
            //noinspection unchecked
            list = (ArrayList<Media>) currentList.clone();
        }

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("lang", filter.langCode));
        params.add(new NameValuePair("sort_by", "rating"));
        //params.add(new NameValuePair("page", Integer.toString(filter.page)));

        Request.Builder requestBuilder = new Request.Builder();
        String query = buildQuery(params);
        requestBuilder.url(API_URLS[CURRENT_API] + query);
        requestBuilder.tag(MEDIA_CALL);

        ArrayList<Person> actors = new ArrayList<>();
        actors.add(new Person("1", "Benedict Cumberbatch", "https://image.tmdb.org/t/p/w640/2NQH6clGUjJmVSOjWiVD54gurKE.jpg", "Doctor Strange"));
        actors.add(new Person("2", "Chiwetel Ejiofor ", "https://image.tmdb.org/t/p/w640/nfpaL5EvWf3C68Uc8Q8UocVNByh.jpg", "Baron Karl Mordo"));
        actors.add(new Person("3", "Rachel McAdams", "https://image.tmdb.org/t/p/w640/c60WxtQceDxOp7sd2iWhOqn5Y2l.jpg", "Christine Palmer"));
        actors.add(new Person("4", "Benedict Wong", "https://image.tmdb.org/t/p/w640/iBzJ8s7GqgtRfGH3q0Ep5OKnaGf.jpg", "Wong"));
        actors.add(new Person("5", "Mads Mikkelsen", "https://image.tmdb.org/t/p/w640/o29Wd1DL8ZcSnVlOhLZ53LPPRwi.jpg", "Kaecilius"));
        actors.add(new Person("6", "Chris Hemsworth", "https://image.tmdb.org/t/p/w640/lrhth7yK9p3vy6p7AabDUM1THKl.jpg", "Thor Odinson"));

        Movie m = new Movie("1", "The Only One Movie Possible", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", "https://image.tmdb.org/t/p/w640/hLudzvGfpi6JlwUnsNhXwKKg4j.jpg", this, "125", "2017", "10.0", "76", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/x3akjRHhZnIZx0EiQ3eOg66qoS9.jpg");
        m.genre = "Drama";
        m.synopsis = "Taking place after alien crafts land around the world, an expert linguist is recruited by the military to determine whether they come in peace or are a threat.";
        m.director = "Denis Villeneuve";
        m.directorImage = "https://image.tmdb.org/t/p/w640/uRzwzBRJVqsFjlrvF53zYJhIJRI.jpg";
        //m.actors = actors;

        Movie m2 = new Movie("1", "Doctor Strange", "https://image.tmdb.org/t/p/w640/xfWac8MTYDxujaxgPVcRD9yZaul.jpg", "https://image.tmdb.org/t/p/w640/xfWac8MTYDxujaxgPVcRD9yZaul.jpg", this, "105", "2016", "10.0", "189", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/sDNhWjd4X7c0oOlClkkwvqVOo45.jpg");
        m2.genre = "Adventure";
        m2.synopsis = "After his career is destroyed, a brilliant but arrogant surgeon gets a new lease on life when a sorcerer takes him under his wing and trains him to defend the world against evil.";
        m2.director = "Scott Derrickson";
        m2.directorImage = "https://image.tmdb.org/t/p/w640/7lh5rL4uMgaNmR6O5794s4b1eB7.jpg";
        m2.actors = actors;

        Movie m3 = new Movie("1", "Captain America: Civil War", "https://image.tmdb.org/t/p/w640/5N20rQURev5CNDcMjHVUZhpoCNC.jpg", "https://image.tmdb.org/t/p/w640/5N20rQURev5CNDcMjHVUZhpoCNC.jpg", this, "120", "2016", "9.5", "102", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/m5O3SZvQ6EgD5XXXLPIP1wLppeW.jpg");
        m3.genre = "Action";
        m3.synopsis = "Following the events of Age of Ultron, the collective governments of the world pass an act designed to regulate all superhuman activity. This polarizes opinion amongst the Avengers, causing two factions to side with Iron Man or Captain America, which causes an epic battle between former allies.";
        m3.director = "Joe Russo";
        m3.directorImage = "https://image.tmdb.org/t/p/w640/5bMVczVDqLJFpfLQZhQ4hhwkSQD.jpg";
        //m3.actors = actors;

        for (int i = 0; i < 15; i++) {
            list.add(m);
            list.add(m2);
            list.add(m3);
        }

        return fetchList(list, requestBuilder, filter, callback);
    }

    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Filters filters, final Callback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //callback.onFailure(e);
                callback.onSuccess(filters, currentList, true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr;

                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(new IOException("Socket Exception"));
                        return;
                    }

                    MovieResponse movies;

                    try {
                        movies = gson.fromJson(responseStr, MovieResponse.class);
                    } catch (Exception e) {
                        onFailure(e);
                    }

                    callback.onSuccess(filters, currentList, true);
                }
            }

            void onFailure(Exception ex) {
                Log.d("MoviesProviderError", "Error: " + ex.toString());
                //callback.onFailure(ex);
                callback.onSuccess(filters, currentList, true);
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback) {
        ArrayList<Media> returnList = new ArrayList<>();
        returnList.add(currentList.get(index));
        callback.onSuccess(null, returnList, true);
        return null;
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_movies_title;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(R.id.movies_trending, Filters.Sort.TRENDING, Filters.Order.DESC, "ALL", R.drawable.filter_trending));
        tabs.add(new NavInfo(R.id.movies_trending, Filters.Sort.TRENDING, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.trending), R.drawable.filter_trending));
        return tabs;
    }

    @Override
    public List<Genre> getGenres() {
        ArrayList<Genre> returnList = new ArrayList<>();
        returnList.add(new Genre(null, R.string.all_genres));
        returnList.add(new Genre("action", R.string.genre_action));
        returnList.add(new Genre("adventure", R.string.genre_adventure));
        returnList.add(new Genre("animation", R.string.genre_animation));
        returnList.add(new Genre("comedy", R.string.genre_comedy));
        returnList.add(new Genre("family", R.string.genre_family));
        returnList.add(new Genre("fantasy", R.string.genre_fantasy));
        returnList.add(new Genre("mystery", R.string.genre_mystery));
        returnList.add(new Genre("romance", R.string.genre_romance));
        returnList.add(new Genre("scifi", R.string.genre_sci_fi));
        return returnList;
    }

    private class MovieResponse {
        public String id;
        public String page;
        public LinkedTreeMap<String, Object> results;
        public String total_pages;
        public String total_results;
    }
}
