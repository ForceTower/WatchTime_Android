package com.watchtime.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.watchtime.R;
import com.watchtime.base.providers.media.models.Person;
import com.watchtime.base.utils.AnimUtils;
import com.watchtime.base.utils.PixelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by João Paulo on 17/02/2017.
 */

public class CastAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public interface OnPersonClickListener {
        void onPersonClick(View v, Person person, int position);
        void onShowMoreClick(View v);
    }

    private OnPersonClickListener personClickListener;

    public void setOnPersonClickListener(OnPersonClickListener personClickListener) {
        this.personClickListener = personClickListener;
    }

    private Context context;
    private List<PersonView> actors;
    private final int MAX_ACTORS_IN_VIEW = 6;
    private final int SHOW_MORE = 0, PERSON = 1;

    public CastAdapter(Context context, ArrayList<Person> actors) {
        this.context = context;
        setItems(actors);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == PERSON) {
            view = LayoutInflater.from(context).inflate(R.layout.actor_cast_item, parent, false);
            return new ActorViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.view_more_cast_item, parent, false);
            return new ShowMoreViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == PERSON) {
            final ActorViewHolder actorVH = (ActorViewHolder) holder;
            final Person actor = getPerson(position);
            actorVH.actorImageView.setVisibility(View.INVISIBLE);
            //actorVH.personName.setVisibility(View.INVISIBLE);
            //actorVH.personRole.setVisibility(View.INVISIBLE);

            //actorVH.personName.setText(actor.name);
            //actorVH.personRole.setText(actor.role);
            if (actor.profileImage != null && !actor.profileImage.isEmpty()) {
                Picasso.with(actorVH.actorImageView.getContext()).load(actor.profileImage).into(actorVH.actorImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        AnimUtils.fadeIn(actorVH.actorImageView);
                        //AnimUtils.fadeIn(actorVH.personName);
                        //AnimUtils.fadeIn(actorVH.personRole);
                    }

                    @Override
                    public void onError() {
                        Log.d("CastAdapter", "Error loading image for " + actor.name);
                    }
                });
            }
        } else {
            final ShowMoreViewHolder showMore = (ShowMoreViewHolder) holder;
        }
    }

    @Override
    public int getItemCount() {
        return actors.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isShowMore)
            return SHOW_MORE;
        return PERSON;
    }

    public void setItems(List<Person> actors) {
        int MAX_ACTORS = MAX_ACTORS_IN_VIEW;

        /*if (!PixelUtils.screenIsPortrait(context)) {
            MAX_ACTORS *= 2;
        }*/

        if (actors.size() >= MAX_ACTORS) {
            actors = actors.subList(0, MAX_ACTORS-1);
        }

        this.actors = new ArrayList<>();
        for (Person person : actors) {
            this.actors.add(new PersonView(person));
        }

        this.actors.add(new PersonView()); //Add the show more;

        notifyDataSetChanged();
    }

    public PersonView getItem(int position) {
        if (position < 0 || position >= actors.size())
            return null;
        return actors.get(position);
    }

    public Person getPerson(int position) {
        if (getItem(position) != null)
            return getItem(position).person;
        return null;
    }

    private class PersonView {
        boolean isShowMore;
        Person person;

        PersonView(Person p) {
            person = p;
            isShowMore = false;
        }

        PersonView() {
            isShowMore = true;
        }
    }

    private class ShowMoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;

        ShowMoreViewHolder(View view) {
            super(view);
            this.itemView = view;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (personClickListener != null) {
                personClickListener.onShowMoreClick(itemView);
            }
        }
    }

    class ActorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;

        @Bind(R.id.person_image)
        CircleImageView actorImageView;
        /*@Bind(R.id.person_name)
        TextView personName;
        @Bind(R.id.person_role)
        TextView personRole;*/

        ActorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;

            itemView.setOnClickListener(this);
        }

        public CircleImageView getActorImageView() {
            return actorImageView;
        }

        @Override
        public void onClick(View v) {
            if (personClickListener != null) {
                int position = getLayoutPosition();
                personClickListener.onPersonClick(v, getPerson(position), position);
            }
        }
    }
}
