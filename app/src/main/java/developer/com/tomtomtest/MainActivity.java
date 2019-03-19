package developer.com.tomtomtest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback.OnMarkerClickListener;
import com.tomtom.online.sdk.map.TomtomMapCallback.OnMapClickListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import developer.com.tomtomtest.Objetos.DBHelper;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, OnMapClickListener, OnMarkerClickListener, Constantes {

    private TomtomMap Mapa;
    private LatLng PosicaoUsuario;
    private boolean mapaPronto = false;

    List<Marker> Pontos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment fragmento = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.Mapa);
        Pontos = new ArrayList<>();

        assert fragmento != null;
        fragmento.getAsyncMap(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Mapa.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        Mapa = tomtomMap;
        Mapa.setMyLocationEnabled(true);
        new PosicaoTask().execute();
        Mapa.addOnMapClickListener(this);
        Mapa.addOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        AlertDialog.Builder Dialogbuilder = new AlertDialog.Builder(this);
        Dialogbuilder.setTitle("Escolha o Icone!");
        Dialogbuilder.setItems(R.array.Icones, (dialog, p) -> {
            Mapa.addMarker(
                    new MarkerBuilder(latLng)
                            .icon(Icon.Factory.fromResources(MainActivity.this, Icons[p]))
                            .tag(p)
            );

            Pontos = Mapa.getMarkers();
        });

        Dialogbuilder.show();
    }

    @Override
    public void onMarkerClick(@NonNull Marker marker) {
        Mapa.removeMarkerByID(marker.getId());
        Pontos.remove(marker);
    }

    @Override
    protected void onStop() {
        super.onStop();
        new DBHelper(this).updatePontos(Pontos);
    }

    public void criarPontoAleatorio(View view) {
        if (mapaPronto) {
            Random gerador = new Random();
            double lat = gerador.nextDouble()*17.08*0.000001 - gerador.nextDouble()*17.08*0.000001;
            double longi = gerador.nextDouble()*5.77612*0.001 - gerador.nextDouble()*5.77612*0.001 ;
            LatLng camera = Mapa.getCenterOfMap();

            LatLng posicao = new LatLng(camera.getLatitude()+lat,camera.getLongitude()+longi);
            int p = gerador.nextInt(Icons.length);
            Mapa.addMarker(
                    new MarkerBuilder(posicao)
                            .icon(Icon.Factory.fromResources(MainActivity.this,Icons[p]))//78 x 90
                            .tag(p)
            );

            Pontos = Mapa.getMarkers();
        } else {
            Toast.makeText(this, "O mapa ainda n esta pronto", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PosicaoTask extends AsyncTask<Void, Void, LatLng> {

        @Override
        protected LatLng doInBackground(Void... voids) {
            while (Mapa.getUserLocation() == null);
            return new LatLng(Mapa.getUserLocation());
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            PosicaoUsuario = latLng;
            ArrayList<MarkerBuilder> marcas = new DBHelper(MainActivity.this).getMarcas();

            for (int i = 0; i < marcas.size(); i++) {
                Mapa.addMarker(marcas.get(i));
            }

            Pontos = Mapa.getMarkers();

            Mapa.centerOnMyLocation();
            mapaPronto = true;
        }
    }
}
