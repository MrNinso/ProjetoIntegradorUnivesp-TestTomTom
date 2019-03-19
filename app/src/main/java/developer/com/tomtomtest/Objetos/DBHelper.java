package developer.com.tomtomtest.Objetos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;

import java.util.ArrayList;
import java.util.List;

import developer.com.tomtomtest.Constantes;

public class DBHelper extends SQLiteOpenHelper implements Constantes {

    private Context Contexto;

    public DBHelper(Context context) {
        super(context, "banco.db", null, 1);
        Contexto = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String marcas = "CREATE TABLE marcas(" +
                "LAT REAL," +
                "LONG REAL," +
                "ICON INTEGER)";

        db.execSQL(marcas);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public ArrayList<MarkerBuilder> getMarcas() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MarkerBuilder> marcas = new ArrayList<>();

        Cursor c = db.rawQuery("SELECT * FROM marcas",null);

        if (c.moveToFirst()) {
            do {
                marcas.add(
                        new MarkerBuilder(
                                new LatLng(c.getDouble(0), c.getDouble(1)))
                                .icon(Icon.Factory.fromResources(Contexto,Icons[c.getInt(2)]))
                                .tag(c.getInt(2))
                );
            } while (c.moveToNext());
        }

        c.close();
        return marcas;
    }

    public void updatePontos(List<Marker> marcas) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM marcas");

        for (int i = 0; i <  marcas.size(); i++) {
            Marker marca = marcas.get(i);
            ContentValues cv = new ContentValues();

            cv.put("LAT", marca.getPosition().getLatitude());
            cv.put("LONG", marca.getPosition().getLongitude());
            cv.put("ICON",(int) marca.getTag());

            db.insert("marcas", null, cv);
        }
    }
}
