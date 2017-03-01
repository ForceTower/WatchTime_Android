package com.watchtime.base.providers.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

import com.watchtime.base.providers.BaseProvider;
import com.watchtime.base.providers.media.models.Genre;
import com.watchtime.base.providers.media.models.Media;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by João Paulo on 24/01/2017.
 *
 * This class provides all the necessary media to fill a View
 */
public abstract class MediaProvider extends BaseProvider implements Parcelable {
    public static final String MEDIA_CALL = "media_http_call";

    public Call getList(Filters filters, Callback callback) {
        return getList(null, filters, callback);
    }
    public abstract Call getList(ArrayList<Media> currentList, Filters filters, Callback callback);
    public List<Genre> getGenres() {
        return new ArrayList<>();
    }
    public abstract Call getDetail(ArrayList<Media> currentList, Integer index, Callback callback);
    public abstract int getLoadingMessage();
    public abstract List<NavInfo> getNavigation();
    public int getDefaultNavigationIndex() {
        return 1;
    }

    public interface Callback {
        void onSuccess(Filters filters, ArrayList<Media> items, boolean changed);
        void onFailure(Exception e);
    }

    public static class Filters {
        public enum Order {ASC, DESC}
        public enum Sort {POPULARITY, RELEASE, NOW_PLAYING, RATING, UPCOMING}

        public String keywords = null;
        public String genre = null;
        public Order order = Order.DESC;
        public Sort sort = Sort.POPULARITY;
        public Integer page = null;
        public String langCode = "en";

        public Filters() { }

        public Filters(Filters filters) {
            keywords = filters.keywords;
            genre = filters.genre;
            order = filters.order;
            sort = filters.sort;
            page = filters.page;
            langCode = filters.langCode;
        }
    }

    public static class NavInfo {
        private final Integer mIconId;
        private int mId;
        private Filters.Sort mSort;
        private Filters.Order mDefOrder;
        private String mLabel;

        NavInfo(int id, Filters.Sort sort, Filters.Order defOrder, String label,@Nullable @DrawableRes Integer icon) {
            mId = id;
            mSort = sort;
            mDefOrder = defOrder;
            mLabel = label;
            mIconId = icon;
        }

        public Filters.Sort getFilter() {
            return mSort;
        }

        public int getId() {
            return mId;
        }

        @DrawableRes
        public int getIcon() {
            return mIconId;
        }

        public Filters.Order getOrder() {
            return mDefOrder;
        }

        public String getLabel() {
            return mLabel;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String className = getClass().getCanonicalName();
        dest.writeString(className);
    }

    public static final Parcelable.Creator<MediaProvider> CREATOR = new Parcelable.Creator<MediaProvider>() {
        @Override
        public MediaProvider createFromParcel(Parcel source) {
            String className = source.readString();
            MediaProvider provider = null;

            try {
                Class<?> clazz = Class.forName(className);
                provider = (MediaProvider)clazz.newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }

            return provider;
        }

        @Override
        public MediaProvider[] newArray(int size) {
            return null;
        }
    };
}
