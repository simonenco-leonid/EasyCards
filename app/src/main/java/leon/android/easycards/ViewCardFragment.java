package leon.android.easycards;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import leon.android.easycards.adapter.CardAdapter;
import leon.android.easycards.database.DatabaseHelper;
import leon.android.easycards.model.Card;

public class ViewCardFragment extends Fragment implements CardAdapter.OnRecyclerListener {
    private static final String TAG = "ViewContactsFragment";

    public interface onCardSelectedListener {
        public void onCardSelected(Card card);
    }

    onCardSelectedListener mCardListener;

    public interface onAddCardListener {
        public void onAddCard();
    }

    onAddCardListener mOnAddCard;

    //variables and widgets
    private static final int STANDARD_APPBAR = 0;
    private static final int SEARCH_APPBAR = 1;
    private int mAppBarState;

    private AppBarLayout viewCardsBar, searchBar;
    private RecyclerView mRecyclerCardView;
    private CardAdapter mAdapter;
    private EditText mSearchCards;
    private List<Card> cards = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_cards, container, false);
        viewCardsBar = (AppBarLayout) rootView.findViewById(R.id.viewCardToolbar);
        searchBar = (AppBarLayout) rootView.findViewById(R.id.searchToolbar);
        mRecyclerCardView = rootView.findViewById(R.id.cardRecyclerView);
        mSearchCards = rootView.findViewById(R.id.etSearchCards);

        setAppBarState(STANDARD_APPBAR);

        initRecyclerView();

        // navigate to add contacts fragment
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fabAddContact);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked fab.");
                mOnAddCard.onAddCard();
            }
        });

        ImageView imageViewSearchIcon = (ImageView) rootView.findViewById(R.id.imageViewSearchIcon);
        imageViewSearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked search icon.");
                toggleToolBarState();
            }
        });

        ImageView imageViewBackArrow = (ImageView) rootView.findViewById(R.id.imageViewBackArrow);
        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked back arrow.");
                toggleToolBarState();
            }
        });


        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCardListener = (onCardSelectedListener) getActivity();
            mOnAddCard = (onAddCardListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerCardView.setLayoutManager(mLayoutManager);
        mRecyclerCardView.setHasFixedSize(true);
        List cards = getAllCards();
        mAdapter = new CardAdapter(getActivity(), cards, "", this);
        mRecyclerCardView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerCardView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerCardView.addItemDecoration(dividerItemDecoration);

    }

    private List<Card> getAllCards() {
        cards = new ArrayList<>();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        Cursor cursor = databaseHelper.getAllCards();

        //iterate through all the rows contained in the database
        if (!cursor.moveToNext()) {
            Toast.makeText(getActivity(), "There are no contacts to show", Toast.LENGTH_SHORT).show();
        }
        while (cursor.moveToNext()) {
            cards.add(new Card(
                    cursor.getString(1),//name
                    cursor.getString(2)// image
            ));
        }


        //sort the arraylist based on the contact name
        Collections.sort(cards, new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                return o1.getNameOfCard().compareToIgnoreCase(o2.getNameOfCard());
            }
        });

        mSearchCards.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String text = mSearchCards.getText().toString().toLowerCase(Locale.getDefault());
                mAdapter.filter(text);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return cards;
    }

    @Override
    public void onClickRecyclerPosition(int position) {
        Log.d(TAG, "onClick: navigating to " + getString(R.string.card_fragment));
        mCardListener.onCardSelected(cards.get(position));
    }

    /**
     * Initiates the appbar state toggle
     */
    private void toggleToolBarState() {
        Log.d(TAG, "toggleToolBarState: toggling AppBarState.");
        if (mAppBarState == STANDARD_APPBAR) {
            setAppBarState(SEARCH_APPBAR);
        } else {
            setAppBarState(STANDARD_APPBAR);
        }
    }


    /**
     * Sets the appbar state for either the search 'mode' or 'standard' mode
     *
     * @param state
     */
    private void setAppBarState(int state) {
        Log.d(TAG, "setAppBarState: changing app bar state to: " + state);

        mAppBarState = state;

        if (mAppBarState == STANDARD_APPBAR) {
            searchBar.setVisibility(View.GONE);
            viewCardsBar.setVisibility(View.VISIBLE);

            //hide the keyboard
            View view = getView();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (NullPointerException e) {
                Log.d(TAG, "setAppBarState: NullPointerException: " + e.getMessage());
            }

        } else if (mAppBarState == SEARCH_APPBAR) {
            viewCardsBar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            mSearchCards.requestFocus();

            //open the keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setAppBarState(STANDARD_APPBAR);
    }
}