package com.example.android.autofill.service.data.source.local.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Callback;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomOpenHelper;
import android.arch.persistence.room.RoomOpenHelper.Delegate;
import android.arch.persistence.room.util.TableInfo;
import android.arch.persistence.room.util.TableInfo.Column;
import android.arch.persistence.room.util.TableInfo.ForeignKey;
import android.arch.persistence.room.util.TableInfo.Index;
import com.example.android.autofill.service.data.source.local.dao.AutofillDao;
import com.example.android.autofill.service.data.source.local.dao.AutofillDao_Impl;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.annotation.Generated;

@Generated("android.arch.persistence.room.RoomProcessor")
public class AutofillDatabase_Impl extends AutofillDatabase {
  private volatile AutofillDao _autofillDao;

  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `FilledAutofillField` (`datasetId` TEXT NOT NULL, `textValue` TEXT, `dateValue` INTEGER, `toggleValue` INTEGER, `fieldTypeName` TEXT NOT NULL, PRIMARY KEY(`datasetId`, `fieldTypeName`), FOREIGN KEY(`datasetId`) REFERENCES `AutofillDataset`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`fieldTypeName`) REFERENCES `FieldType`(`typeName`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `AutofillDataset` (`id` TEXT NOT NULL, `datasetName` TEXT NOT NULL, `packageName` TEXT NOT NULL, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `FieldType` (`typeName` TEXT NOT NULL, `autofillTypes` TEXT NOT NULL, `saveInfo` INTEGER NOT NULL, `partition` INTEGER NOT NULL, `strictExampleSet` TEXT, `textTemplate` TEXT, `dateTemplate` TEXT, PRIMARY KEY(`typeName`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `AutofillHint` (`autofillHint` TEXT NOT NULL, `fieldTypeName` TEXT NOT NULL, PRIMARY KEY(`autofillHint`), FOREIGN KEY(`fieldTypeName`) REFERENCES `FieldType`(`typeName`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `ResourceIdHeuristic` (`resourceIdHeuristic` TEXT NOT NULL, `packageName` TEXT NOT NULL, `fieldTypeName` TEXT NOT NULL, PRIMARY KEY(`resourceIdHeuristic`, `packageName`), FOREIGN KEY(`fieldTypeName`) REFERENCES `FieldType`(`typeName`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"06ccd47c7fde55992e976484caacf9b1\")");
      }

      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `FilledAutofillField`");
        _db.execSQL("DROP TABLE IF EXISTS `AutofillDataset`");
        _db.execSQL("DROP TABLE IF EXISTS `FieldType`");
        _db.execSQL("DROP TABLE IF EXISTS `AutofillHint`");
        _db.execSQL("DROP TABLE IF EXISTS `ResourceIdHeuristic`");
      }

      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        _db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsFilledAutofillField = new HashMap<String, TableInfo.Column>(5);
        _columnsFilledAutofillField.put("datasetId", new TableInfo.Column("datasetId", "TEXT", true, 1));
        _columnsFilledAutofillField.put("textValue", new TableInfo.Column("textValue", "TEXT", false, 0));
        _columnsFilledAutofillField.put("dateValue", new TableInfo.Column("dateValue", "INTEGER", false, 0));
        _columnsFilledAutofillField.put("toggleValue", new TableInfo.Column("toggleValue", "INTEGER", false, 0));
        _columnsFilledAutofillField.put("fieldTypeName", new TableInfo.Column("fieldTypeName", "TEXT", true, 2));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFilledAutofillField = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysFilledAutofillField.add(new TableInfo.ForeignKey("AutofillDataset", "CASCADE", "NO ACTION",Arrays.asList("datasetId"), Arrays.asList("id")));
        _foreignKeysFilledAutofillField.add(new TableInfo.ForeignKey("FieldType", "CASCADE", "NO ACTION",Arrays.asList("fieldTypeName"), Arrays.asList("typeName")));
        final HashSet<TableInfo.Index> _indicesFilledAutofillField = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFilledAutofillField = new TableInfo("FilledAutofillField", _columnsFilledAutofillField, _foreignKeysFilledAutofillField, _indicesFilledAutofillField);
        final TableInfo _existingFilledAutofillField = TableInfo.read(_db, "FilledAutofillField");
        if (! _infoFilledAutofillField.equals(_existingFilledAutofillField)) {
          throw new IllegalStateException("Migration didn't properly handle FilledAutofillField(com.example.android.autofill.service.model.FilledAutofillField).\n"
                  + " Expected:\n" + _infoFilledAutofillField + "\n"
                  + " Found:\n" + _existingFilledAutofillField);
        }
        final HashMap<String, TableInfo.Column> _columnsAutofillDataset = new HashMap<String, TableInfo.Column>(3);
        _columnsAutofillDataset.put("id", new TableInfo.Column("id", "TEXT", true, 1));
        _columnsAutofillDataset.put("datasetName", new TableInfo.Column("datasetName", "TEXT", true, 0));
        _columnsAutofillDataset.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAutofillDataset = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAutofillDataset = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAutofillDataset = new TableInfo("AutofillDataset", _columnsAutofillDataset, _foreignKeysAutofillDataset, _indicesAutofillDataset);
        final TableInfo _existingAutofillDataset = TableInfo.read(_db, "AutofillDataset");
        if (! _infoAutofillDataset.equals(_existingAutofillDataset)) {
          throw new IllegalStateException("Migration didn't properly handle AutofillDataset(com.example.android.autofill.service.model.AutofillDataset).\n"
                  + " Expected:\n" + _infoAutofillDataset + "\n"
                  + " Found:\n" + _existingAutofillDataset);
        }
        final HashMap<String, TableInfo.Column> _columnsFieldType = new HashMap<String, TableInfo.Column>(7);
        _columnsFieldType.put("typeName", new TableInfo.Column("typeName", "TEXT", true, 1));
        _columnsFieldType.put("autofillTypes", new TableInfo.Column("autofillTypes", "TEXT", true, 0));
        _columnsFieldType.put("saveInfo", new TableInfo.Column("saveInfo", "INTEGER", true, 0));
        _columnsFieldType.put("partition", new TableInfo.Column("partition", "INTEGER", true, 0));
        _columnsFieldType.put("strictExampleSet", new TableInfo.Column("strictExampleSet", "TEXT", false, 0));
        _columnsFieldType.put("textTemplate", new TableInfo.Column("textTemplate", "TEXT", false, 0));
        _columnsFieldType.put("dateTemplate", new TableInfo.Column("dateTemplate", "TEXT", false, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFieldType = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFieldType = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFieldType = new TableInfo("FieldType", _columnsFieldType, _foreignKeysFieldType, _indicesFieldType);
        final TableInfo _existingFieldType = TableInfo.read(_db, "FieldType");
        if (! _infoFieldType.equals(_existingFieldType)) {
          throw new IllegalStateException("Migration didn't properly handle FieldType(com.example.android.autofill.service.model.FieldType).\n"
                  + " Expected:\n" + _infoFieldType + "\n"
                  + " Found:\n" + _existingFieldType);
        }
        final HashMap<String, TableInfo.Column> _columnsAutofillHint = new HashMap<String, TableInfo.Column>(2);
        _columnsAutofillHint.put("autofillHint", new TableInfo.Column("autofillHint", "TEXT", true, 1));
        _columnsAutofillHint.put("fieldTypeName", new TableInfo.Column("fieldTypeName", "TEXT", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAutofillHint = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysAutofillHint.add(new TableInfo.ForeignKey("FieldType", "CASCADE", "NO ACTION",Arrays.asList("fieldTypeName"), Arrays.asList("typeName")));
        final HashSet<TableInfo.Index> _indicesAutofillHint = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAutofillHint = new TableInfo("AutofillHint", _columnsAutofillHint, _foreignKeysAutofillHint, _indicesAutofillHint);
        final TableInfo _existingAutofillHint = TableInfo.read(_db, "AutofillHint");
        if (! _infoAutofillHint.equals(_existingAutofillHint)) {
          throw new IllegalStateException("Migration didn't properly handle AutofillHint(com.example.android.autofill.service.model.AutofillHint).\n"
                  + " Expected:\n" + _infoAutofillHint + "\n"
                  + " Found:\n" + _existingAutofillHint);
        }
        final HashMap<String, TableInfo.Column> _columnsResourceIdHeuristic = new HashMap<String, TableInfo.Column>(3);
        _columnsResourceIdHeuristic.put("resourceIdHeuristic", new TableInfo.Column("resourceIdHeuristic", "TEXT", true, 1));
        _columnsResourceIdHeuristic.put("packageName", new TableInfo.Column("packageName", "TEXT", true, 2));
        _columnsResourceIdHeuristic.put("fieldTypeName", new TableInfo.Column("fieldTypeName", "TEXT", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysResourceIdHeuristic = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysResourceIdHeuristic.add(new TableInfo.ForeignKey("FieldType", "CASCADE", "NO ACTION",Arrays.asList("fieldTypeName"), Arrays.asList("typeName")));
        final HashSet<TableInfo.Index> _indicesResourceIdHeuristic = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoResourceIdHeuristic = new TableInfo("ResourceIdHeuristic", _columnsResourceIdHeuristic, _foreignKeysResourceIdHeuristic, _indicesResourceIdHeuristic);
        final TableInfo _existingResourceIdHeuristic = TableInfo.read(_db, "ResourceIdHeuristic");
        if (! _infoResourceIdHeuristic.equals(_existingResourceIdHeuristic)) {
          throw new IllegalStateException("Migration didn't properly handle ResourceIdHeuristic(com.example.android.autofill.service.model.ResourceIdHeuristic).\n"
                  + " Expected:\n" + _infoResourceIdHeuristic + "\n"
                  + " Found:\n" + _existingResourceIdHeuristic);
        }
      }
    }, "06ccd47c7fde55992e976484caacf9b1");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    return new InvalidationTracker(this, "FilledAutofillField","AutofillDataset","FieldType","AutofillHint","ResourceIdHeuristic");
  }

  @Override
  public AutofillDao autofillDao() {
    if (_autofillDao != null) {
      return _autofillDao;
    } else {
      synchronized(this) {
        if(_autofillDao == null) {
          _autofillDao = new AutofillDao_Impl(this);
        }
        return _autofillDao;
      }
    }
  }
}
