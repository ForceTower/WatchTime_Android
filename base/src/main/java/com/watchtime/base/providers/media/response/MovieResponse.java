package com.watchtime.base.providers.media.response;

import com.watchtime.base.providers.media.MediaProvider;
import com.watchtime.base.providers.media.models.Media;
import com.watchtime.base.providers.media.models.Movie;
import com.watchtime.base.providers.media.models.Person;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MovieResponse {
    private JSONArray response;
    private MediaProvider mediaProvider;

    public MovieResponse(String json, MediaProvider mediaProvider) throws JSONException {
        this.response = new JSONArray(json);
        this.mediaProvider = mediaProvider;
    }

    public MovieResponse(MediaProvider mediaProvider) {
        this.mediaProvider = mediaProvider;
    }

    public ArrayList<Movie> asList() throws JSONException {
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

    public ArrayList<Media> singleMovie(JSONObject item) throws Exception {
        if (item.has("error")) {
            throw new Exception("Invalid Request");
        }

        JSONArray data = item.getJSONArray("data");
        item = data.getJSONObject(0);

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


        HashMap<String, Integer> genres = new HashMap<>();

        JSONObject genresObj = item.getJSONObject("genres");
        JSONArray dataGenres = genresObj.getJSONArray("data");
        String firstGenre = "";

        for (int i = 0; i < dataGenres.length(); i++) {
            JSONObject current = dataGenres.getJSONObject(i);
            genres.put(current.getString("genre"), current.getInt("genre_id"));
            if (i == 0) firstGenre = current.getString("genre");
        }

        movie.allGenres = genres;
        if (!genres.isEmpty())
            movie.genre = firstGenre;

        JSONObject cast = item.optJSONObject("cast");
        if (cast != null)
            movie.actors = parseActors(cast);

        JSONObject crew = item.optJSONObject("crew");
        if (crew != null) {
            ArrayList<String> director = getDirectorName(crew);
            movie.director = director.get(0);
            movie.directorImage = director.get(1);
        }

        JSONObject backdrops = item.optJSONObject("images");
        if (backdrops != null) {
            movie.backdrops = parseBackdrops(backdrops);
        }

        returnList.add(movie);
        return returnList;
    }

    private ArrayList<Person> parseActors(JSONObject cast) throws JSONException {
        ArrayList<Person> returnList = new ArrayList<>();
        JSONArray data = cast.getJSONArray("data");

        for (int i = 0; i < data.length(); i++) {
            JSONObject pers = data.getJSONObject(i);

            String tmdb = pers.getString("tmdb");
            String name = pers.getString("person");
            String prof = pers.optString("profile_path", "");
            String role = pers.getString("character");

            if (!prof.trim().isEmpty()) {
                prof = "https://image.tmdb.org/t/p/w185/" + prof;
            }

            Person person = new Person(tmdb, name, prof, role);
            returnList.add(person);
        }

        return returnList;
    }

    private ArrayList<String> getDirectorName(JSONObject crew) throws JSONException {
        ArrayList<String> returnList = new ArrayList<>();
        JSONArray data = crew.getJSONArray("data");

        String directorName = "Unknown";
        String profilePicture = "";

        for (int i = 0; i < data.length(); i++) {
            JSONObject pers = data.getJSONObject(i);

            String tmdb = pers.getString("tmdb");
            String name = pers.getString("person");
            String prof = pers.optString("profile_path", "");
            String role = pers.optString("job", "");

            if (role.equals("Director")) {
                directorName = name;
                profilePicture = "https://image.tmdb.org/t/p/w185/" + prof;
            }
        }

        returnList.add(directorName);
        returnList.add(profilePicture);
        return returnList;
    }

    private HashMap<String, Integer> parseBackdrops(JSONObject object) throws JSONException {
        HashMap<String, Integer> backdrops = new HashMap<>();

        JSONArray array = object.optJSONArray("data");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject backdrop = array.getJSONObject(i);
                String url = backdrop.optString("image_path");
                int id = backdrop.getInt("id");
                if (url != null)
                    backdrops.put("https://image.tmdb.org/t/p/w780" + url, id);
            }
        }

        return backdrops;
    }
}