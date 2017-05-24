package com.example.frankie.homies.frags;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.frankie.homies.Appunti;
import com.example.frankie.homies.R;
import com.example.frankie.homies.activities.Home;
import com.example.frankie.homies.classes.Homie;
import com.example.frankie.homies.classes.HomieAction;
import com.example.frankie.homies.classes.House;
import com.example.frankie.homies.utility.QuickAddDialog;
import com.example.frankie.homies.utility.adapters.ActionAdapter;
import com.example.frankie.homies.utility.adapters.HomiesBuilder;
import com.example.frankie.homies.utility.views.CircleImageView;
import com.example.frankie.homies.utility.views.RoundedImageView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class HomeFrag extends Fragment {

    private Homie who;
    public CircularImageView profilePicture;
    public CircleImageView casetta;
    private QuickAddDialog addDialog;
    public Bitmap profilePictureBitmap;
    private House house;
    private ListView homeListView;
    private String addressUpdateRequest;
    private ActionAdapter toFill;
    private FloatingActionButton picFab;
    private double amount;


    private OnFragmentInteractionListener mListener;

    public HomeFrag() {
        // Required empty public constructor
    }

    public static HomeFrag newInstance(Homie user , House house) {
        HomeFrag fragment = new HomeFrag();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        args.putSerializable("house", house);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            who = (Homie)getArguments().getSerializable("user");
            house = (House)getArguments().getSerializable("house");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;


    }

    public void onViewCreated(View view , Bundle savedInstanceState){
        addressUpdateRequest=getString(R.string.address)+"updateExpenses.php";

        TextView provaUsername =(TextView) getView().findViewById(R.id.textHomeProva);
        provaUsername.setText("Ciao "+who.getUserName());
        CardView cardView=(CardView)getView().findViewById(R.id.toShowOrNotToShow);
        if(who.getHouseID()==0)
            cardView.setVisibility(View.GONE);

        amount=who.getAccountAmount();
        Log.d("DEBUG" , "amount "+amount+ "query "+who.getAccountAmount());
        TextView provaAccount = (TextView)getView().findViewById(R.id.accountInfoTextView);
        provaAccount.setText("Il tuo conto è di "+who.getAccountAmount()+"€");
        TextView provaNomeCognome = (TextView)getView().findViewById(R.id.provaNomeCognome);
        provaNomeCognome.setText(who.getName()+" "+who.getSurname());
        if(amount<0)
            provaAccount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        else if(amount==0)
        provaAccount.setTextColor(getResources().getColor(R.color.colorPrimary));
        else
            provaAccount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        CardView userCardView = (CardView) getView().findViewById(R.id.userCard);
        userCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Ciao "+who.getName()+" "+who.getSurname(), Toast.LENGTH_LONG).show();
            }
        });

        profilePicture= (CircularImageView)getView().findViewById(R.id.profilePicView);

        profilePicture.setBorderColor(getResources().getColor(R.color.colorPrimaryDark));
        Picasso.with(getContext()).load(getString(R.string.addressProfilePic)+who.getUserName()+who.getUserID()+".png").into(profilePicture);
        profilePicture.setBorderWidth(8);
        profilePicture.setShadowRadius(1.8f);
        profilePicture.setShadowColor(getResources().getColor(R.color.black));
        profilePicture.setElevation(10);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(2);
            }
        });

        casetta = (CircleImageView) getView().findViewById(R.id.houseProfPic);
        casetta.setBorderColor(getResources().getColor(R.color.colorPrimaryDark));
        casetta.setBorderWidth(8);
        casetta.setElevation(10);
        casetta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(3);
            }
        });
        TextView houseText = (TextView)getView().findViewById(R.id.infoPreview);
        final TextView houseAmount = (TextView)getView().findViewById(R.id.houseAmount);
        if(who.getHouseID()==0) {
            houseText.setText("Non hai ancora un gruppo Casa. \n Creane uno");
            houseText.setTextSize(20);
        }
        else {
            if (house.getHouseAmount() < 0)
                houseAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            else
                houseAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            houseAmount.setText("Il saldo della casa è " + house.getHouseAmount() + "€");
            houseText.setText(house.getHouseName());

            homeListView = (ListView) getView().findViewById(R.id.homeFragListView);
            /*
            final ArrayList<String> prova = new ArrayList<>();

            for (HomieAction action:house.getHomieActions()) {
                prova.add(""+action.getReason()+":"+action.getHomieActionAmount()+"€ , Fatta da "+action.getUsername());
            }
            */
            
            toFill = new ActionAdapter(this.getContext().getApplicationContext(), R.layout.row_action_adapter, house.getHomieActions());
            homeListView.setAdapter(toFill);
            homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HomieAction  action  = (HomieAction)homeListView.getItemAtPosition(position);
                    // Show Toast
                    Dialog info  = new Dialog(getContext());
                    info.setTitle("Spesa");
                    info.setContentView(R.layout.info_homefrag);
                    TextView perche = (TextView)info.findViewById(R.id.perche);
                    TextView chi = (TextView)info.findViewById(R.id.dachi);
                    TextView debito = (TextView)info.findViewById(R.id.debito);
                    TextView quanto = (TextView)info.findViewById(R.id.quanto);
                    perche.setText(action.getReason());
                    chi.setText("Fatta da "+action.getUsername());
                    quanto.setText("Costo:"+action.getHomieActionAmount()+"€");
                    if(action.getUsername().equals(who.getUserName())){
                        double debt = action.getHomieActionAmount()/house.getHomies().size()*(house.getHomies().size()-1);
                        debito.setTextColor(ContextCompat.getColor(getContext() , R.color.green));
                        debito.setText("Ti devono "+Appunti.arrotonda(debt,2)+"€");
                    }
                    else{
                        double debt = action.getHomieActionAmount()/house.getHomies().size();
                        debito.setTextColor(ContextCompat.getColor(getContext() , R.color.red));
                        debito.setText("Gli devi "+ Appunti.arrotonda(debt,2)+"€");
                    }

                    info.show();


                }
            });
            homeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    final HomieAction  action  = (HomieAction)homeListView.getItemAtPosition(position);
                    if(who.getUserName().equals(action.getUsername())){
                        addDialog = new QuickAddDialog(getContext());
                        addDialog.setContentView(R.layout.fragment_add_quick);
                        addDialog.setCancelable(true);
                        final TextInputEditText reason = (TextInputEditText) addDialog.findViewById(R.id.aqreason);
                        final TextInputEditText amount = (TextInputEditText) addDialog.findViewById(R.id.aqamount);
                        final CheckBox checkBox =(CheckBox)addDialog.findViewById(R.id.addExpCheck);
                        reason.setText(action.getReason());
                        amount.setText(action.getHomieActionAmount()+"");
                        final SimpleDateFormat today = new SimpleDateFormat();
                        checkBox.setText(today.format(new Date()).toString());
                        checkBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkBox.setSelected(false);
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    final DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
                                    datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatePicker datePicker = datePickerDialog.getDatePicker();
                                            Date tocheck = new Date(datePicker.getYear() , datePicker.getMonth()+1 , datePicker.getDayOfMonth());
                                            // Date insertDate = new Date(datePicker.getDayOfMonth(),(datePicker.getMonth()+1),datePicker.getYear());
                                            if(!tocheck.after(new Date())){
                                                String toSet = datePicker.getDayOfMonth()+"/"+(datePicker.getMonth()+1)+"/"+datePicker.getYear();
                                                checkBox.setText(toSet);
                                                checkBox.setChecked(true);
                                                datePickerDialog.dismiss();
                                            }
                                            else {
                                                checkBox.setChecked(true);
                                                final TextView error = (TextView)addDialog.findViewById(R.id.error);
                                                error.setText("Non puoi scegliere questa data");
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        error.setText("");
                                                    }
                                                }, 3000);
                                                datePickerDialog.dismiss();
                                            }


                                        }
                                    });
                                    datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Annulla", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            checkBox.setChecked(true);
                                            datePickerDialog.dismiss();
                                        }
                                    });

                                    datePickerDialog.show();

                                }
                                else{
                                    final Dialog chooseDateDialog = new Dialog(getContext());
                                    chooseDateDialog.setContentView(R.layout.minsdk_datepicker);
                                    final DatePicker datePickerLowSDK= (DatePicker) chooseDateDialog.findViewById(R.id.datePickerlowSDK);
                                    Button nope = (Button)chooseDateDialog.findViewById(R.id.lowSDKNope);
                                    Button yep = (Button)chooseDateDialog.findViewById(R.id.lowSDKYes);
                                    nope.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            chooseDateDialog.dismiss();
                                        }
                                    });
                                    yep.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Date tocheck = new Date(datePickerLowSDK.getYear() , datePickerLowSDK.getMonth()+1 , datePickerLowSDK.getDayOfMonth());
                                            // Date insertDate = new Date(datePicker.getDayOfMonth(),(datePicker.getMonth()+1),datePicker.getYear());
                                            if(!tocheck.after(new Date())){
                                                String toSet = datePickerLowSDK.getDayOfMonth()+"/"+(datePickerLowSDK.getMonth()+1)+"/"+datePickerLowSDK.getYear();
                                                checkBox.setText(toSet);
                                                checkBox.setChecked(true);
                                                chooseDateDialog.dismiss();
                                            }
                                            else {
                                                checkBox.setChecked(true);
                                                final TextView error = (TextView)addDialog.findViewById(R.id.error);
                                                error.setText("Non puoi scegliere questa data");
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        error.setText("");
                                                    }
                                                }, 3000);
                                                chooseDateDialog.dismiss();
                                            }
                                        }
                                    });
                                    chooseDateDialog.show();
                                }


                            }
                        });
                        picFab = (FloatingActionButton)addDialog.findViewById(R.id.addPicToExpenses);

                        TextView insert = (TextView) addDialog.findViewById(R.id.goToInsertExpenses);
                        insert.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!reason.getText().toString().equals("")) {
                                    if (!amount.equals("")) {

                                        String reasonString = reason.getText().toString();
                                        Log.d("DEBUG", amount.getText().toString() + " " + reason.getText().toString());

                                            updateHomieAction(addressUpdateRequest, Double.parseDouble(amount.getText().toString()), reasonString, action.getHouseIDExpenses() , action.getHouseID());





                                    } else {
                                        amount.setError(getString(R.string.invalid_amount));
                                        amount.requestFocus();
                                    }
                                } else {
                                    reason.setError(getString(R.string.invalid_reason));
                                    reason.requestFocus();
                                }

                            }
                        });
                        addDialog.show();
                    }
                    return true;
                }
            });
        }



    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
          } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void pickImage(int type) {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivityForResult(intent, type);
        }
    }
    public void updateHomieAction(String address, final double amount, final String reason, final Integer expID, final Integer houseID) {
        Log.d("DEBUG", "Aggiungo la spesa ");
        com.android.volley.RequestQueue queue = Volley.newRequestQueue(getContext());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, address,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("DEBUG", "update "+response+"update");
                        try {
                            house = HomiesBuilder.buildHouse(house, response);
                            homeListView = (ListView) getView().findViewById(R.id.homeFragListView);
                            toFill = new ActionAdapter(getContext(), R.layout.row_action_adapter, house.getHomieActions());
                            homeListView.setAdapter(toFill);
                            addDialog.dismiss();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DEBUG", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> my_map = new HashMap<>();
                my_map.put("amount", amount + "");
                my_map.put("reason", reason);
                my_map.put("houseid" ,houseID+"");
                my_map.put("expid" , expID+"");


                return my_map;
            }
        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }








}
