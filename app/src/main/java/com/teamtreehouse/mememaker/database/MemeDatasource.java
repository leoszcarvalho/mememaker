package com.teamtreehouse.mememaker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.teamtreehouse.mememaker.models.Meme;
import com.teamtreehouse.mememaker.models.MemeAnnotation;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Evan Anger on 8/17/14.
 */
public class MemeDatasource {

    private Context mContext;
    private MemeSQLiteHelper mMemeSqlLiteHelper;

    public MemeDatasource(Context context)
    {
        mContext = context;
        mMemeSqlLiteHelper = new MemeSQLiteHelper(context);
        SQLiteDatabase database = mMemeSqlLiteHelper.getReadableDatabase();
        database.close();
    }

    public ArrayList<Meme> read()
    {
        return null;
    }

    public ArrayList<Meme> readMemes()
    {
        SQLiteDatabase database = open();

        Cursor cursor = database.query(MemeSQLiteHelper.MEMES_TABLE,
                new String[]{MemeSQLiteHelper.COLUMN_MEME_NAME, BaseColumns._ID, MemeSQLiteHelper.COLUMN_MEME_ASSET},
                null,   //selection
                null,   //selection args
                null,   //groupby
                null,   //having
                null);  //order

        ArrayList<Meme> memes = new ArrayList<Meme>();

        if(cursor.moveToFirst())
        {
            do
            {
                Meme meme = new Meme(getIntFromColumnName(cursor, BaseColumns._ID),
                        getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_MEME_ASSET),
                        getStringFromColumnName(cursor, MemeSQLiteHelper.COLUMN_MEME_NAME),
                        null);
                memes.add(meme);
            }
            while(cursor.moveToNext());
        }
        cursor.close();

        close(database);

        return memes;

    }


    private int getIntFromColumnName(Cursor cursor, String columnName)
    {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }

    private String getStringFromColumnName(Cursor cursor, String columnName)
    {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);

    }

    public void create(Meme meme)
    {
        SQLiteDatabase db = open();
        db.beginTransaction();
        //implementation details
        ContentValues memeValues = new ContentValues();
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_NAME, meme.getName());
        memeValues.put(MemeSQLiteHelper.COLUMN_MEME_ASSET, meme.getAssetLocation());
        long memeId = db.insert(MemeSQLiteHelper.MEMES_TABLE, null, memeValues);

        //Percorrimento do array para inserir as diversas anotações sobre o mesmo meme
        for(MemeAnnotation annotation : meme.getAnnotations())
        {
            ContentValues annotationValues = new ContentValues();
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_COLOR, annotation.getColor());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_TITLE, annotation.getTitle());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_X, annotation.getLocationX());
            annotationValues.put(MemeSQLiteHelper.COLUMN_ANNOTATION_Y, annotation.getLocationY());
            annotationValues.put(MemeSQLiteHelper.COLUMN_FOREIGN_KEY_MEME, memeId);

            db.insert(MemeSQLiteHelper.ANNOTATIONS_TABLE, null, annotationValues);

        }

        db.setTransactionSuccessful();
        db.endTransaction();
        close(db);
    }

    private void close(SQLiteDatabase db)
    {
        db.close();
    }

    private SQLiteDatabase open()
    {
        return mMemeSqlLiteHelper.getWritableDatabase();
    }

}
