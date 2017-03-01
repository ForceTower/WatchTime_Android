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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by João Paulo on 16/02/2017.
 */

public class MoviesProvider extends MediaProvider{
    private static final MoviesProvider mediaProvider = new MoviesProvider();
    private static final String API_URL = ApiEndPoints.BASE_MOVIES_POPULAR;
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

        String requestWebsite = ApiEndPoints.BASE_MOVIES_POPULAR;
        if (filter.sort == Filters.Sort.RELEASE) {
            requestWebsite = ApiEndPoints.BASE_MOVIES_RELEASE;
        } else if (filter.sort == Filters.Sort.RATING) {
            requestWebsite = ApiEndPoints.BASE_MOVIES_RATING;
        } else if (filter.sort == Filters.Sort.NOW_PLAYING) {
            requestWebsite = ApiEndPoints.BASE_MOVIES_NOW_PLAYING;
        } else if (filter.sort == Filters.Sort.UPCOMING) {
            requestWebsite = ApiEndPoints.BASE_MOVIES_UPCOMING;
        }

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(requestWebsite + filter.page);
        requestBuilder.tag(MEDIA_CALL);

        ArrayList<Person> actors = new ArrayList<>();
        actors.add(new Person("1", "Benedict Cumberbatch", "https://image.tmdb.org/t/p/w640/2NQH6clGUjJmVSOjWiVD54gurKE.jpg", "Doctor Strange"));
        actors.add(new Person("2", "Chiwetel Ejiofor ", "https://image.tmdb.org/t/p/w640/nfpaL5EvWf3C68Uc8Q8UocVNByh.jpg", "Baron Karl Mordo"));
        actors.add(new Person("3", "Rachel McAdams", "https://image.tmdb.org/t/p/w640/c60WxtQceDxOp7sd2iWhOqn5Y2l.jpg", "Christine Palmer"));
        actors.add(new Person("4", "Benedict Wong", "https://image.tmdb.org/t/p/w640/iBzJ8s7GqgtRfGH3q0Ep5OKnaGf.jpg", "Wong"));
        actors.add(new Person("5", "Mads Mikkelsen", "https://image.tmdb.org/t/p/w640/o29Wd1DL8ZcSnVlOhLZ53LPPRwi.jpg", "Kaecilius"));
        actors.add(new Person("6", "Chris Hemsworth", "https://image.tmdb.org/t/p/w640/lrhth7yK9p3vy6p7AabDUM1THKl.jpg", "Thor Odinson"));

        Movie m2 = new Movie("1", "Doctor Strange", "https://image.tmdb.org/t/p/w640/xfWac8MTYDxujaxgPVcRD9yZaul.jpg", "https://image.tmdb.org/t/p/w640/xfWac8MTYDxujaxgPVcRD9yZaul.jpg", this, "105", "2016", "10.0", "189", "https://image.tmdb.org/t/p/w533_and_h300_bestv2/sDNhWjd4X7c0oOlClkkwvqVOo45.jpg");
        m2.genre = "Adventure";
        m2.synopsis = "After his career is destroyed, a brilliant but arrogant surgeon gets a new lease on life when a sorcerer takes him under his wing and trains him to defend the world against evil.";
        m2.director = "Scott Derrickson";
        m2.directorImage = "https://image.tmdb.org/t/p/w640/7lh5rL4uMgaNmR6O5794s4b1eB7.jpg";
        m2.actors = actors;

        return fetchList(list, requestBuilder, filter, callback);
    }

    private Call fetchList(final ArrayList<Media> currentList, final Request.Builder requestBuilder, final Filters filters, final Callback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr;

                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(e);
                        return;
                    }

                    MovieResponse movies;
                    try {
                        movies = new MovieResponse(responseStr);
                        currentList.addAll(movies.asList());
                    } catch (Exception e) {
                        onFailure(e);
                    }
                    callback.onSuccess(filters, currentList, true);
                } else {
                    onFailure(new Exception("Response is Unsuccessful"));
                }
            }

            void onFailure(Exception ex) {
                Log.d("MoviesProviderError", "Error: " + ex.toString());
                callback.onFailure(ex);
            }
        });
    }

    @Override
    public Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback) {
        Media media = currentList.get(index);
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(ApiEndPoints.BASE_MOVIES_DETAILS + media.videoId);
        requestBuilder.tag(MEDIA_CALL);
        return fetchDetails(requestBuilder, callback);
    }

    private Call fetchDetails(final Request.Builder requestBuilder, final Callback callback) {
        return enqueue(requestBuilder.build(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("MoviesProviderError", "Failed Fetching Details");
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("MoviesProvider", "Details Response Arrived");

                if (response.isSuccessful()) {
                    String responseStr;
                    try {
                        responseStr = response.body().string();
                    } catch (SocketException e) {
                        onFailure(e);
                        return;
                    }

                    MovieResponse movieResponse;
                    try {
                        movieResponse = new MovieResponse();
                        JSONObject json = new JSONObject(responseStr);
                        callback.onSuccess(null, movieResponse.singleMovie(json), true);
                    } catch (Exception e) {
                        onFailure(e);
                    }

                }
            }

            void onFailure(Exception ex) {
                Log.d("MoviesProviderError", "Error: " + ex.toString());
                callback.onFailure(ex);
            }
        });
    }

    @Override
    public int getLoadingMessage() {
        return R.string.loading_movies_title;
    }

    @Override
    public List<NavInfo> getNavigation() {
        List<NavInfo> tabs = new ArrayList<>();
        tabs.add(new NavInfo(R.id.movies_release, Filters.Sort.RELEASE, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.release), R.drawable.filter_release));
        tabs.add(new NavInfo(R.id.movies_popular, Filters.Sort.POPULARITY, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.popularity), R.drawable.filter_popularity));
        tabs.add(new NavInfo(R.id.movies_rating, Filters.Sort.RATING, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.rating), R.drawable.filter_rating));
        tabs.add(new NavInfo(R.id.movies_now_playing, Filters.Sort.NOW_PLAYING, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.now_playing), R.drawable.filter_now_playing));
        tabs.add(new NavInfo(R.id.movies_upcoming, Filters.Sort.UPCOMING, Filters.Order.DESC, WatchTimeApplication.getAppContext().getString(R.string.upcoming), R.drawable.filter_upcoming));
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
        JSONArray response;

        MovieResponse(String json) throws JSONException {
            this.response = new JSONArray(json);
        }

        MovieResponse() {}

        ArrayList<Movie> asList() throws JSONException {
            ArrayList<Movie> list = new ArrayList<>();
            if(response == null)
                return list;

            for (int i = 0; i < response.length(); i++) {
                JSONObject item = response.getJSONObject(i);

                Integer tmdb_id = item.getInt("tmdb");
                String name = item.getString("name");
                String overview = item.optString("overview", "");
                String release_date = item.optString("release_date", "0000-00-00");
                String poster = item.optString("poster_path", "");
                String backdrop = item.optString("backdrop_path", "");
                String rating = item.getString("vote_average");
                String reviews = item.getString("vote_count");
                String runtime = item.optString("runtime", "0");

                String year = release_date.split("-")[0];
                if (!poster.trim().equals("")) {
                    poster = "https://image.tmdb.org/t/p/w780/" + poster;
                }

                if (!backdrop.trim().equals("")) {
                    backdrop = "https://image.tmdb.org/t/p/w780/" + backdrop;
                }

                if (runtime == null || runtime.equals("null"))
                    runtime = "0";

                Movie movie = new Movie(tmdb_id.toString(), name, poster, poster, mediaProvider, runtime, year, rating, reviews, backdrop);
                movie.synopsis = overview;
                list.add(movie);
            }

            return list;
        }

        ArrayList<Media> singleMovie(JSONObject item) throws JSONException {
            ArrayList<Media> returnList = new ArrayList<>();

            Integer tmdb_id = item.getInt("tmdb");
            String name = item.getString("name");
            String tag_line = item.optString("tag_line", "");
            String overview = item.optString("overview", "");
            String imdb = item.optString("imdb", "");
            String status = item.optString("status", "Unknown");
            String release_date = item.optString("release_date", "0000-00-00");
            String poster = item.optString("poster_path", "");
            String backdrop = item.optString("backdrop_path", "");
            String rating = item.getString("vote_average");
            String reviews = item.getString("vote_count");
            String runtime = item.optString("runtime", "0");
            Integer budget = item.optInt("budget", 0);
            Integer revenue = item.optInt("revenue", 0);
            String homepage = item.optString("homepage", "");

            String year = release_date.split("-")[0];
            if (!poster.trim().equals("")) {
                poster = "https://image.tmdb.org/t/p/w780/" + poster;
            }

            if (!backdrop.trim().equals("")) {
                backdrop = "https://image.tmdb.org/t/p/w780/" + backdrop;
            }

            if (runtime == null || runtime.equals("null"))
                runtime = "0";

            Movie movie = new Movie(tmdb_id.toString(), name, poster, poster, mediaProvider, runtime, year, rating, reviews, backdrop);
            movie.synopsis = overview;

            returnList.add(movie);
            Log.d("MoviesProvider", "Executed");
            return returnList;
        }
    }
}
