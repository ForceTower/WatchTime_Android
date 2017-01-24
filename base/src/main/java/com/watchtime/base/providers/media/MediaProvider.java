package com.watchtime.base.providers.media;

import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

import com.squareup.okhttp.Call;
import com.watchtime.base.providers.BaseProvider;
import com.watchtime.base.providers.media.models.Media;

import java.util.ArrayList;

/**
 * Created by João Paulo on 24/01/2017.
 *
 * This class provides all the necessary media to fill a View
 */
public abstract class MediaProvider extends BaseProvider implements Parcelable {
    public interface Callback {
        void onSuccess(Filters filters, ArrayList<Media> items, boolean changed);

        void onFailure(Exception e);
    }

    public static class Filters {
        public enum Order {ASC, DESC};
        public enum Sort {POPULARITY, YEAR, DATE, RATING, ALPHABET, TRENDING}

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

        public NavInfo(int id,Filters.Sort sort, Filters.Order defOrder, String label,@Nullable @DrawableRes Integer icon) {
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

    public static final String MEDIA_CALL = "media_http_call";

    public Call getList(Filters filters, Callback callback) {
        return getList(null, filters, callback);
    }

    public abstract Call getList(ArrayList<Media> currentList, Filters filters, Callback callback);
}
