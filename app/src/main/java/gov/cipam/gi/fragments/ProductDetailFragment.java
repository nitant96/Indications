package gov.cipam.gi.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Window;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gov.cipam.gi.activities.MapsActivity;
import gov.cipam.gi.R;
import gov.cipam.gi.activities.WebViewActivity;
import gov.cipam.gi.adapters.SellerListAdapter;
import gov.cipam.gi.database.Database;
import gov.cipam.gi.model.Product;
import gov.cipam.gi.model.Seller;
import gov.cipam.gi.utils.CommonUtils;
import gov.cipam.gi.utils.PaletteGenerate;


/**
 * Created by karan on 12/14/2017.
 */

public class ProductDetailFragment extends Fragment implements SellerListAdapter.setOnSellerClickListener {
//    Seller seller;
//    SellerFirebaseAdapter sellerFirebaseAdapter;
//    DatabaseReference mDatabaseReference;
    TextView txtState,txtCategory;
    String name,address,contact;
    Double lon,lat;
    RecyclerView sellerRecyclerView;
    ArrayList<Seller> sellerList;
    SellerListAdapter sellerListAdapter;
    Database databaseInstance;
    SQLiteDatabase database;
    ImageView imageView;
    PaletteGenerate paletteGenerate;
    Product product;
    Toolbar toolbar;
    Window window;
    ExpandableTextView expTv1;
    boolean isImagePreLoaded = false;
    public static Bitmap mBitmap;

 
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    public ProductDetailFragment() {
    }
  
 
    public static ProductDetailFragment newInstance(Product product, Bitmap bitmap) {

        Bundle args = new Bundle();
        args.putSerializable("nalin", product);
        ProductDetailFragment fragment = new ProductDetailFragment();
        fragment.setArguments(args);
        mBitmap=bitmap;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        sellerList = new ArrayList<>();
        databaseInstance = new Database(getContext());
        database = databaseInstance.getReadableDatabase();
        paletteGenerate=new PaletteGenerate();
        toolbar=((AppCompatActivity) getActivity()).findViewById(R.id.product_list_toolbar);
        window=((AppCompatActivity) getActivity()).getWindow();
        paletteGenerate.setToolbarColor(mBitmap,toolbar,window);
        populateSellerListFromDB();
        setHasOptionsMenu(true);

        sellerListAdapter = new SellerListAdapter(getContext(), sellerList, this);
//        seller=new Seller();
//        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Giproducts").child("agrPalakkadanMattaRice").child("seller");
//        sellerFirebaseAdapter= new SellerFirebaseAdapter(getContext(),Seller.class,R.layout.card_view_seller_item, SellerViewHolder.class,mDatabaseReference);

        super.onCreate(savedInstanceState);
    }

    private void populateSellerListFromDB() {

//        String[] s={ProductDetailActivity.currentProduct.getUid()};
  
        Bundle b=getArguments();
        product=(Product)b.get("nalin");


        String[] s={product.getUid()};
        Cursor sellerCursor=database.query(Database.GI_SELLER_TABLE,null,Database.GI_SELLER_UID+"=?",s,null,null,null,null);

        while(sellerCursor.moveToNext()){


            name=sellerCursor.getString(sellerCursor.getColumnIndex(Database.GI_SELLER_NAME));
            address=sellerCursor.getString(sellerCursor.getColumnIndex(Database.GI_SELLER_ADDRESS));
            contact=sellerCursor.getString(sellerCursor.getColumnIndex(Database.GI_SELLER_CONTACT));
            lat=sellerCursor.getDouble(sellerCursor.getColumnIndex(Database.GI_SELLER_LAT));
            lon=sellerCursor.getDouble(sellerCursor.getColumnIndex(Database.GI_SELLER_LON));

            Seller oneSeller = new Seller(name, address, contact, lon, lat);

            sellerList.add(oneSeller);
        }
        sellerCursor.close();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem settingsAction=menu.findItem(R.id.action_settings_product_list);
        settingsAction.setVisible(false);
        MenuItem refreshOption=menu.findItem(R.id.menu_refresh);
        refreshOption.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        switch (id){
            case android.R.id.home:
                getActivity().getSupportFragmentManager()
                        .popBackStackImmediate();
                break;
            case R.id.action_location:
                startActivity(new Intent(getContext(),MapsActivity.class)
                        .putExtra("latitude",lat)
                        .putExtra("longitude",lon)
                        .putExtra("address",address));
                break;
            case R.id.action_url:
                Toast.makeText(getContext(),R.string.url,Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), WebViewActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

  
        sellerRecyclerView= view.findViewById(R.id.seller_recycler_view);
        imageView=view.findViewById(R.id.productDetailImage);
        txtState=view.findViewById(R.id.detail_stateName);
        txtCategory=view.findViewById(R.id.detail_categoryName);
        expTv1 = view.findViewById(R.id.expand_text_view);


//        imageView=getSharedElementEnterTransition()
        sellerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
  
        sellerRecyclerView = view.findViewById(R.id.seller_recycler_view);
        if(mBitmap!=null) {
            imageView.setImageBitmap(mBitmap);
        }
        else{
            Picasso.with(getContext()).load(product.getDpurl()).fit().into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap bitmap;
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    bitmap = drawable.getBitmap();
                    mBitmap=bitmap;
                    paletteGenerate.setToolbarColor(mBitmap,toolbar,window);
                }

                @Override
                public void onError() {

                }
            });
        }
//        CommonUtils.loadImage(imageView,mBitmap,getActivity());
//        imageView=getSharedElementEnterTransition()
        sellerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
 
//        sellerRecyclerView.setAdapter(sellerFirebaseAdapter);
        sellerRecyclerView.setAdapter(sellerListAdapter);
        //sellerRecyclerView.addItemDecoration(new android.support.v7.widget.DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        expTv1.setText(product.getDetail());
        txtCategory.setText(product.getCategory());
        txtState.setText(product.getState());
        //expTv1.setText(ProductDetailActivity.currentProduct.getDetail());
    }

    @Override
    public void onSellerClicked(View v, int Position) {

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }
}
