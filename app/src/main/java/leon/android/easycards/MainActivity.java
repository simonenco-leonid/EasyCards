package leon.android.easycards;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;

import leon.android.easycards.model.Card;
import leon.android.easycards.utils.UniversalImageLoader;

public class MainActivity extends AppCompatActivity implements
        ViewCardFragment.onCardSelectedListener,
        CardFragment.onEditCardListener,
        ViewCardFragment.onAddCardListener {

    private static final String TAG = "MainActivity";


    @Override
    public void onEditCardSelected(Card card) {
        Log.d(TAG, "onEditCardSelected: card selected from "
                + getString(R.string.edit_card_fragment)
                + " " + card.getNameOfCard());

        EditCardFragment fragment = new EditCardFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.card), card);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(getString(R.string.edit_card_fragment));
        transaction.commit();
    }

    @Override
    public void onCardSelected(Card card) {
        Log.d(TAG, "onCardSelected: contact selected from "
                + getString(R.string.view_card_fragment)
                + " " + card.getNameOfCard());

        CardFragment cardFragment = new CardFragment();
        ViewCardFragment viewCardFragment = new ViewCardFragment();

        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.card), card);
        cardFragment.setArguments(args);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Inflate transitions to apply
            Transition changeTransform = TransitionInflater.from(this).
                    inflateTransition(R.transition.change_image_transform);
            Transition explodeTransform = TransitionInflater.from(this).
                    inflateTransition(android.R.transition.explode);

            // Setup exit transition on first fragment
            viewCardFragment.setSharedElementReturnTransition(changeTransform);
            viewCardFragment.setExitTransition(explodeTransform);

            // Setup enter transition on second fragment
            cardFragment.setSharedElementEnterTransition(changeTransform);
            cardFragment.setEnterTransition(explodeTransform);

            // Find the shared element (in Fragment A)
            ImageView cardImage = (ImageView) findViewById(R.id.cardImage);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, cardFragment);
            transaction.addToBackStack(getString(R.string.card_fragment));
            transaction.addSharedElement(cardImage, "profile");
            transaction.commit();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, cardFragment);
            transaction.addToBackStack(getString(R.string.card_fragment));
            transaction.commit();
        }
    }

    @Override
    public void onAddCard() {
        Log.d(TAG, "onAddCard: navigating to " + getString(R.string.add_card_fragment));

        AddCardFragment fragment = new AddCardFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(getString(R.string.add_card_fragment));
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initImageLoader();

        init();
    }

    /**
     * initialize the first fragment (ViewContactsFragment)
     */
    private void init() {
        ViewCardFragment fragment = new ViewCardFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // replace what ever is in the fragment_container view with this fragment,
        // amd add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment);
        // transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(MainActivity.this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /**
     * Compress a bitmap by the @param "quality"
     * Quality can be anywhere from 1-100 : 100 being the highest quality.
     *
     * @param bitmap
     * @param quality
     * @return
     */
    public Bitmap compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return bitmap;
    }


}
