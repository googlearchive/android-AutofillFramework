package com.example.android.autofill.service.data.source.local.dao;

import android.arch.persistence.db.SupportSQLiteStatement;
import android.arch.persistence.room.EntityInsertionAdapter;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.RoomSQLiteQuery;
import android.arch.persistence.room.SharedSQLiteStatement;
import android.arch.persistence.room.util.StringUtil;
import android.database.Cursor;
import android.support.v4.util.ArrayMap;
import com.example.android.autofill.service.data.source.local.db.Converters;
import com.example.android.autofill.service.model.AutofillDataset;
import com.example.android.autofill.service.model.AutofillHint;
import com.example.android.autofill.service.model.DatasetWithFilledAutofillFields;
import com.example.android.autofill.service.model.FakeData;
import com.example.android.autofill.service.model.FieldType;
import com.example.android.autofill.service.model.FieldTypeWithHeuristics;
import com.example.android.autofill.service.model.FilledAutofillField;
import com.example.android.autofill.service.model.ResourceIdHeuristic;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;

@Generated("android.arch.persistence.room.RoomProcessor")
public class AutofillDao_Impl implements AutofillDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfFilledAutofillField;

  private final EntityInsertionAdapter __insertionAdapterOfAutofillDataset;

  private final EntityInsertionAdapter __insertionAdapterOfAutofillHint;

  private final EntityInsertionAdapter __insertionAdapterOfResourceIdHeuristic;

  private final EntityInsertionAdapter __insertionAdapterOfFieldType;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public AutofillDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFilledAutofillField = new EntityInsertionAdapter<FilledAutofillField>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `FilledAutofillField`(`datasetId`,`textValue`,`dateValue`,`toggleValue`,`fieldTypeName`) VALUES (?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, FilledAutofillField value) {
        if (value.getDatasetId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getDatasetId());
        }
        if (value.getTextValue() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getTextValue());
        }
        if (value.getDateValue() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, value.getDateValue());
        }
        final Integer _tmp;
        _tmp = value.getToggleValue() == null ? null : (value.getToggleValue() ? 1 : 0);
        if (_tmp == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindLong(4, _tmp);
        }
        if (value.getFieldTypeName() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getFieldTypeName());
        }
      }
    };
    this.__insertionAdapterOfAutofillDataset = new EntityInsertionAdapter<AutofillDataset>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `AutofillDataset`(`id`,`datasetName`,`packageName`) VALUES (?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AutofillDataset value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
        if (value.getDatasetName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getDatasetName());
        }
        if (value.getPackageName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getPackageName());
        }
      }
    };
    this.__insertionAdapterOfAutofillHint = new EntityInsertionAdapter<AutofillHint>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `AutofillHint`(`autofillHint`,`fieldTypeName`) VALUES (?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AutofillHint value) {
        if (value.mAutofillHint == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.mAutofillHint);
        }
        if (value.mFieldTypeName == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.mFieldTypeName);
        }
      }
    };
    this.__insertionAdapterOfResourceIdHeuristic = new EntityInsertionAdapter<ResourceIdHeuristic>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `ResourceIdHeuristic`(`resourceIdHeuristic`,`packageName`,`fieldTypeName`) VALUES (?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, ResourceIdHeuristic value) {
        if (value.mResourceIdHeuristic == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.mResourceIdHeuristic);
        }
        if (value.mPackageName == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.mPackageName);
        }
        if (value.mFieldTypeName == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.mFieldTypeName);
        }
      }
    };
    this.__insertionAdapterOfFieldType = new EntityInsertionAdapter<FieldType>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `FieldType`(`typeName`,`autofillTypes`,`saveInfo`,`partition`,`strictExampleSet`,`textTemplate`,`dateTemplate`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, FieldType value) {
        if (value.getTypeName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getTypeName());
        }
        final String _tmp;
        _tmp = Converters.intListToStoredString(value.getAutofillTypes());
        if (_tmp == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, _tmp);
        }
        if (value.getSaveInfo() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, value.getSaveInfo());
        }
        if (value.getPartition() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindLong(4, value.getPartition());
        }
        final FakeData _tmpMFakeData = value.getFakeData();
        if(_tmpMFakeData != null) {
          final String _tmp_1;
          _tmp_1 = Converters.stringListToStoredString(_tmpMFakeData.strictExampleSet);
          if (_tmp_1 == null) {
            stmt.bindNull(5);
          } else {
            stmt.bindString(5, _tmp_1);
          }
          if (_tmpMFakeData.textTemplate == null) {
            stmt.bindNull(6);
          } else {
            stmt.bindString(6, _tmpMFakeData.textTemplate);
          }
          if (_tmpMFakeData.dateTemplate == null) {
            stmt.bindNull(7);
          } else {
            stmt.bindString(7, _tmpMFakeData.dateTemplate);
          }
        } else {
          stmt.bindNull(5);
          stmt.bindNull(6);
          stmt.bindNull(7);
        }
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM AutofillDataset";
        return _query;
      }
    };
  }

  @Override
  public void insertFilledAutofillFields(Collection<FilledAutofillField> autofillFields) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfFilledAutofillField.insert(autofillFields);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAutofillDataset(AutofillDataset datasets) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfAutofillDataset.insert(datasets);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAutofillHints(List<AutofillHint> autofillHints) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfAutofillHint.insert(autofillHints);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertResourceIdHeuristic(ResourceIdHeuristic resourceIdHeuristic) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfResourceIdHeuristic.insert(resourceIdHeuristic);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertFieldTypes(List<FieldType> fieldTypes) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfFieldType.insert(fieldTypes);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void clearAll() {
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfClearAll.release(_stmt);
    }
  }

  @Override
  public List<DatasetWithFilledAutofillFields> getDatasets(List<String> allAutofillHints) {
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT DISTINCT id, datasetName FROM FilledAutofillField, AutofillDataset WHERE AutofillDataset.id = FilledAutofillField.datasetId AND FilledAutofillField.fieldTypeName IN (");
    final int _inputSize = allAutofillHints.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : allAutofillHints) {
      if (_item == null) {
        _statement.bindNull(_argIndex);
      } else {
        _statement.bindString(_argIndex, _item);
      }
      _argIndex ++;
    }
    final Cursor _cursor = __db.query(_statement);
    try {
      final ArrayMap<String, ArrayList<FilledAutofillField>> _collectionFilledAutofillFields = new ArrayMap<String, ArrayList<FilledAutofillField>>();
      final int _cursorIndexOfMId = _cursor.getColumnIndexOrThrow("id");
      final int _cursorIndexOfMDatasetName = _cursor.getColumnIndexOrThrow("datasetName");
      final List<DatasetWithFilledAutofillFields> _result = new ArrayList<DatasetWithFilledAutofillFields>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final DatasetWithFilledAutofillFields _item_1;
        final AutofillDataset _tmpAutofillDataset;
        if (! (_cursor.isNull(_cursorIndexOfMId) && _cursor.isNull(_cursorIndexOfMDatasetName))) {
          final String _tmpMId;
          _tmpMId = _cursor.getString(_cursorIndexOfMId);
          final String _tmpMDatasetName;
          _tmpMDatasetName = _cursor.getString(_cursorIndexOfMDatasetName);
          _tmpAutofillDataset = new AutofillDataset(_tmpMId,_tmpMDatasetName,null);
        }  else  {
          _tmpAutofillDataset = null;
        }
        _item_1 = new DatasetWithFilledAutofillFields();
        if (!_cursor.isNull(_cursorIndexOfMId)) {
          final String _tmpKey = _cursor.getString(_cursorIndexOfMId);
          ArrayList<FilledAutofillField> _tmpCollection = _collectionFilledAutofillFields.get(_tmpKey);
          if(_tmpCollection == null) {
            _tmpCollection = new ArrayList<FilledAutofillField>();
            _collectionFilledAutofillFields.put(_tmpKey, _tmpCollection);
          }
          _item_1.filledAutofillFields = _tmpCollection;
        }
        _item_1.autofillDataset = _tmpAutofillDataset;
        _result.add(_item_1);
      }
      __fetchRelationshipFilledAutofillFieldAscomExampleAndroidAutofillServiceModelFilledAutofillField(_collectionFilledAutofillFields);
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<DatasetWithFilledAutofillFields> getAllDatasets() {
    final String _sql = "SELECT DISTINCT id, datasetName FROM FilledAutofillField, AutofillDataset WHERE AutofillDataset.id = FilledAutofillField.datasetId";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = __db.query(_statement);
    try {
      final ArrayMap<String, ArrayList<FilledAutofillField>> _collectionFilledAutofillFields = new ArrayMap<String, ArrayList<FilledAutofillField>>();
      final int _cursorIndexOfMId = _cursor.getColumnIndexOrThrow("id");
      final int _cursorIndexOfMDatasetName = _cursor.getColumnIndexOrThrow("datasetName");
      final List<DatasetWithFilledAutofillFields> _result = new ArrayList<DatasetWithFilledAutofillFields>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final DatasetWithFilledAutofillFields _item;
        final AutofillDataset _tmpAutofillDataset;
        if (! (_cursor.isNull(_cursorIndexOfMId) && _cursor.isNull(_cursorIndexOfMDatasetName))) {
          final String _tmpMId;
          _tmpMId = _cursor.getString(_cursorIndexOfMId);
          final String _tmpMDatasetName;
          _tmpMDatasetName = _cursor.getString(_cursorIndexOfMDatasetName);
          _tmpAutofillDataset = new AutofillDataset(_tmpMId,_tmpMDatasetName,null);
        }  else  {
          _tmpAutofillDataset = null;
        }
        _item = new DatasetWithFilledAutofillFields();
        if (!_cursor.isNull(_cursorIndexOfMId)) {
          final String _tmpKey = _cursor.getString(_cursorIndexOfMId);
          ArrayList<FilledAutofillField> _tmpCollection = _collectionFilledAutofillFields.get(_tmpKey);
          if(_tmpCollection == null) {
            _tmpCollection = new ArrayList<FilledAutofillField>();
            _collectionFilledAutofillFields.put(_tmpKey, _tmpCollection);
          }
          _item.filledAutofillFields = _tmpCollection;
        }
        _item.autofillDataset = _tmpAutofillDataset;
        _result.add(_item);
      }
      __fetchRelationshipFilledAutofillFieldAscomExampleAndroidAutofillServiceModelFilledAutofillField(_collectionFilledAutofillFields);
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<DatasetWithFilledAutofillFields> getDatasetsWithName(List<String> fieldTypes,
      String datasetName) {
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT DISTINCT id, datasetName FROM FilledAutofillField, AutofillDataset WHERE AutofillDataset.id = FilledAutofillField.datasetId AND AutofillDataset.datasetName = (");
    _stringBuilder.append("?");
    _stringBuilder.append(") AND FilledAutofillField.fieldTypeName IN (");
    final int _inputSize = fieldTypes.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 1 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    if (datasetName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, datasetName);
    }
    _argIndex = 2;
    for (String _item : fieldTypes) {
      if (_item == null) {
        _statement.bindNull(_argIndex);
      } else {
        _statement.bindString(_argIndex, _item);
      }
      _argIndex ++;
    }
    final Cursor _cursor = __db.query(_statement);
    try {
      final ArrayMap<String, ArrayList<FilledAutofillField>> _collectionFilledAutofillFields = new ArrayMap<String, ArrayList<FilledAutofillField>>();
      final int _cursorIndexOfMId = _cursor.getColumnIndexOrThrow("id");
      final int _cursorIndexOfMDatasetName = _cursor.getColumnIndexOrThrow("datasetName");
      final List<DatasetWithFilledAutofillFields> _result = new ArrayList<DatasetWithFilledAutofillFields>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final DatasetWithFilledAutofillFields _item_1;
        final AutofillDataset _tmpAutofillDataset;
        if (! (_cursor.isNull(_cursorIndexOfMId) && _cursor.isNull(_cursorIndexOfMDatasetName))) {
          final String _tmpMId;
          _tmpMId = _cursor.getString(_cursorIndexOfMId);
          final String _tmpMDatasetName;
          _tmpMDatasetName = _cursor.getString(_cursorIndexOfMDatasetName);
          _tmpAutofillDataset = new AutofillDataset(_tmpMId,_tmpMDatasetName,null);
        }  else  {
          _tmpAutofillDataset = null;
        }
        _item_1 = new DatasetWithFilledAutofillFields();
        if (!_cursor.isNull(_cursorIndexOfMId)) {
          final String _tmpKey = _cursor.getString(_cursorIndexOfMId);
          ArrayList<FilledAutofillField> _tmpCollection = _collectionFilledAutofillFields.get(_tmpKey);
          if(_tmpCollection == null) {
            _tmpCollection = new ArrayList<FilledAutofillField>();
            _collectionFilledAutofillFields.put(_tmpKey, _tmpCollection);
          }
          _item_1.filledAutofillFields = _tmpCollection;
        }
        _item_1.autofillDataset = _tmpAutofillDataset;
        _result.add(_item_1);
      }
      __fetchRelationshipFilledAutofillFieldAscomExampleAndroidAutofillServiceModelFilledAutofillField(_collectionFilledAutofillFields);
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<FieldTypeWithHeuristics> getFieldTypesWithHints() {
    final String _sql = "SELECT DISTINCT typeName, autofillTypes, saveInfo, partition, strictExampleSet, textTemplate, dateTemplate FROM FieldType, AutofillHint WHERE FieldType.typeName = AutofillHint.fieldTypeName UNION SELECT DISTINCT typeName, autofillTypes, saveInfo, partition, strictExampleSet, textTemplate, dateTemplate FROM FieldType, ResourceIdHeuristic WHERE FieldType.typeName = ResourceIdHeuristic.fieldTypeName";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = __db.query(_statement);
    try {
      final ArrayMap<String, ArrayList<AutofillHint>> _collectionAutofillHints = new ArrayMap<String, ArrayList<AutofillHint>>();
      final ArrayMap<String, ArrayList<ResourceIdHeuristic>> _collectionResourceIdHeuristics = new ArrayMap<String, ArrayList<ResourceIdHeuristic>>();
      final int _cursorIndexOfMTypeName = _cursor.getColumnIndexOrThrow("typeName");
      final int _cursorIndexOfMAutofillTypes = _cursor.getColumnIndexOrThrow("autofillTypes");
      final int _cursorIndexOfMSaveInfo = _cursor.getColumnIndexOrThrow("saveInfo");
      final int _cursorIndexOfMPartition = _cursor.getColumnIndexOrThrow("partition");
      final int _cursorIndexOfStrictExampleSet = _cursor.getColumnIndexOrThrow("strictExampleSet");
      final int _cursorIndexOfTextTemplate = _cursor.getColumnIndexOrThrow("textTemplate");
      final int _cursorIndexOfDateTemplate = _cursor.getColumnIndexOrThrow("dateTemplate");
      final List<FieldTypeWithHeuristics> _result = new ArrayList<FieldTypeWithHeuristics>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final FieldTypeWithHeuristics _item;
        final FieldType _tmpFieldType;
        if (! (_cursor.isNull(_cursorIndexOfMTypeName) && _cursor.isNull(_cursorIndexOfMAutofillTypes) && _cursor.isNull(_cursorIndexOfMSaveInfo) && _cursor.isNull(_cursorIndexOfMPartition) && _cursor.isNull(_cursorIndexOfStrictExampleSet) && _cursor.isNull(_cursorIndexOfTextTemplate) && _cursor.isNull(_cursorIndexOfDateTemplate))) {
          final String _tmpMTypeName;
          _tmpMTypeName = _cursor.getString(_cursorIndexOfMTypeName);
          final Converters.IntList _tmpMAutofillTypes;
          final String _tmp;
          _tmp = _cursor.getString(_cursorIndexOfMAutofillTypes);
          _tmpMAutofillTypes = Converters.storedStringToIntList(_tmp);
          final Integer _tmpMSaveInfo;
          if (_cursor.isNull(_cursorIndexOfMSaveInfo)) {
            _tmpMSaveInfo = null;
          } else {
            _tmpMSaveInfo = _cursor.getInt(_cursorIndexOfMSaveInfo);
          }
          final Integer _tmpMPartition;
          if (_cursor.isNull(_cursorIndexOfMPartition)) {
            _tmpMPartition = null;
          } else {
            _tmpMPartition = _cursor.getInt(_cursorIndexOfMPartition);
          }
          final FakeData _tmpMFakeData;
          final Converters.StringList _tmpStrictExampleSet;
          final String _tmp_1;
          _tmp_1 = _cursor.getString(_cursorIndexOfStrictExampleSet);
          _tmpStrictExampleSet = Converters.storedStringToStringList(_tmp_1);
          final String _tmpTextTemplate;
          _tmpTextTemplate = _cursor.getString(_cursorIndexOfTextTemplate);
          final String _tmpDateTemplate;
          _tmpDateTemplate = _cursor.getString(_cursorIndexOfDateTemplate);
          _tmpMFakeData = new FakeData(_tmpStrictExampleSet,_tmpTextTemplate,_tmpDateTemplate);
          _tmpFieldType = new FieldType(_tmpMTypeName,_tmpMAutofillTypes,_tmpMSaveInfo,_tmpMPartition,_tmpMFakeData);
        }  else  {
          _tmpFieldType = null;
        }
        _item = new FieldTypeWithHeuristics();
        if (!_cursor.isNull(_cursorIndexOfMTypeName)) {
          final String _tmpKey = _cursor.getString(_cursorIndexOfMTypeName);
          ArrayList<AutofillHint> _tmpCollection = _collectionAutofillHints.get(_tmpKey);
          if(_tmpCollection == null) {
            _tmpCollection = new ArrayList<AutofillHint>();
            _collectionAutofillHints.put(_tmpKey, _tmpCollection);
          }
          _item.autofillHints = _tmpCollection;
        }
        if (!_cursor.isNull(_cursorIndexOfMTypeName)) {
          final String _tmpKey_1 = _cursor.getString(_cursorIndexOfMTypeName);
          ArrayList<ResourceIdHeuristic> _tmpCollection_1 = _collectionResourceIdHeuristics.get(_tmpKey_1);
          if(_tmpCollection_1 == null) {
            _tmpCollection_1 = new ArrayList<ResourceIdHeuristic>();
            _collectionResourceIdHeuristics.put(_tmpKey_1, _tmpCollection_1);
          }
          _item.resourceIdHeuristics = _tmpCollection_1;
        }
        _item.fieldType = _tmpFieldType;
        _result.add(_item);
      }
      __fetchRelationshipAutofillHintAscomExampleAndroidAutofillServiceModelAutofillHint(_collectionAutofillHints);
      __fetchRelationshipResourceIdHeuristicAscomExampleAndroidAutofillServiceModelResourceIdHeuristic(_collectionResourceIdHeuristics);
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<FieldTypeWithHeuristics> getFieldTypesForAutofillHints(List<String> autofillHints) {
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT DISTINCT typeName, autofillTypes, saveInfo, partition, strictExampleSet, textTemplate, dateTemplate FROM FieldType, AutofillHint WHERE FieldType.typeName = AutofillHint.fieldTypeName AND AutofillHint.autofillHint IN (");
    final int _inputSize = autofillHints.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") UNION SELECT DISTINCT typeName, autofillTypes, saveInfo, partition, strictExampleSet, textTemplate, dateTemplate FROM FieldType, ResourceIdHeuristic WHERE FieldType.typeName = ResourceIdHeuristic.fieldTypeName");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : autofillHints) {
      if (_item == null) {
        _statement.bindNull(_argIndex);
      } else {
        _statement.bindString(_argIndex, _item);
      }
      _argIndex ++;
    }
    final Cursor _cursor = __db.query(_statement);
    try {
      final ArrayMap<String, ArrayList<AutofillHint>> _collectionAutofillHints = new ArrayMap<String, ArrayList<AutofillHint>>();
      final ArrayMap<String, ArrayList<ResourceIdHeuristic>> _collectionResourceIdHeuristics = new ArrayMap<String, ArrayList<ResourceIdHeuristic>>();
      final int _cursorIndexOfMTypeName = _cursor.getColumnIndexOrThrow("typeName");
      final int _cursorIndexOfMAutofillTypes = _cursor.getColumnIndexOrThrow("autofillTypes");
      final int _cursorIndexOfMSaveInfo = _cursor.getColumnIndexOrThrow("saveInfo");
      final int _cursorIndexOfMPartition = _cursor.getColumnIndexOrThrow("partition");
      final int _cursorIndexOfStrictExampleSet = _cursor.getColumnIndexOrThrow("strictExampleSet");
      final int _cursorIndexOfTextTemplate = _cursor.getColumnIndexOrThrow("textTemplate");
      final int _cursorIndexOfDateTemplate = _cursor.getColumnIndexOrThrow("dateTemplate");
      final List<FieldTypeWithHeuristics> _result = new ArrayList<FieldTypeWithHeuristics>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final FieldTypeWithHeuristics _item_1;
        final FieldType _tmpFieldType;
        if (! (_cursor.isNull(_cursorIndexOfMTypeName) && _cursor.isNull(_cursorIndexOfMAutofillTypes) && _cursor.isNull(_cursorIndexOfMSaveInfo) && _cursor.isNull(_cursorIndexOfMPartition) && _cursor.isNull(_cursorIndexOfStrictExampleSet) && _cursor.isNull(_cursorIndexOfTextTemplate) && _cursor.isNull(_cursorIndexOfDateTemplate))) {
          final String _tmpMTypeName;
          _tmpMTypeName = _cursor.getString(_cursorIndexOfMTypeName);
          final Converters.IntList _tmpMAutofillTypes;
          final String _tmp;
          _tmp = _cursor.getString(_cursorIndexOfMAutofillTypes);
          _tmpMAutofillTypes = Converters.storedStringToIntList(_tmp);
          final Integer _tmpMSaveInfo;
          if (_cursor.isNull(_cursorIndexOfMSaveInfo)) {
            _tmpMSaveInfo = null;
          } else {
            _tmpMSaveInfo = _cursor.getInt(_cursorIndexOfMSaveInfo);
          }
          final Integer _tmpMPartition;
          if (_cursor.isNull(_cursorIndexOfMPartition)) {
            _tmpMPartition = null;
          } else {
            _tmpMPartition = _cursor.getInt(_cursorIndexOfMPartition);
          }
          final FakeData _tmpMFakeData;
          final Converters.StringList _tmpStrictExampleSet;
          final String _tmp_1;
          _tmp_1 = _cursor.getString(_cursorIndexOfStrictExampleSet);
          _tmpStrictExampleSet = Converters.storedStringToStringList(_tmp_1);
          final String _tmpTextTemplate;
          _tmpTextTemplate = _cursor.getString(_cursorIndexOfTextTemplate);
          final String _tmpDateTemplate;
          _tmpDateTemplate = _cursor.getString(_cursorIndexOfDateTemplate);
          _tmpMFakeData = new FakeData(_tmpStrictExampleSet,_tmpTextTemplate,_tmpDateTemplate);
          _tmpFieldType = new FieldType(_tmpMTypeName,_tmpMAutofillTypes,_tmpMSaveInfo,_tmpMPartition,_tmpMFakeData);
        }  else  {
          _tmpFieldType = null;
        }
        _item_1 = new FieldTypeWithHeuristics();
        if (!_cursor.isNull(_cursorIndexOfMTypeName)) {
          final String _tmpKey = _cursor.getString(_cursorIndexOfMTypeName);
          ArrayList<AutofillHint> _tmpCollection = _collectionAutofillHints.get(_tmpKey);
          if(_tmpCollection == null) {
            _tmpCollection = new ArrayList<AutofillHint>();
            _collectionAutofillHints.put(_tmpKey, _tmpCollection);
          }
          _item_1.autofillHints = _tmpCollection;
        }
        if (!_cursor.isNull(_cursorIndexOfMTypeName)) {
          final String _tmpKey_1 = _cursor.getString(_cursorIndexOfMTypeName);
          ArrayList<ResourceIdHeuristic> _tmpCollection_1 = _collectionResourceIdHeuristics.get(_tmpKey_1);
          if(_tmpCollection_1 == null) {
            _tmpCollection_1 = new ArrayList<ResourceIdHeuristic>();
            _collectionResourceIdHeuristics.put(_tmpKey_1, _tmpCollection_1);
          }
          _item_1.resourceIdHeuristics = _tmpCollection_1;
        }
        _item_1.fieldType = _tmpFieldType;
        _result.add(_item_1);
      }
      __fetchRelationshipAutofillHintAscomExampleAndroidAutofillServiceModelAutofillHint(_collectionAutofillHints);
      __fetchRelationshipResourceIdHeuristicAscomExampleAndroidAutofillServiceModelResourceIdHeuristic(_collectionResourceIdHeuristics);
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public DatasetWithFilledAutofillFields getAutofillDatasetWithId(String datasetId) {
    final String _sql = "SELECT DISTINCT id, datasetName FROM FilledAutofillField, AutofillDataset WHERE AutofillDataset.id = FilledAutofillField.datasetId AND AutofillDataset.id = (?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (datasetId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, datasetId);
    }
    final Cursor _cursor = __db.query(_statement);
    try {
      final ArrayMap<String, ArrayList<FilledAutofillField>> _collectionFilledAutofillFields = new ArrayMap<String, ArrayList<FilledAutofillField>>();
      final int _cursorIndexOfMId = _cursor.getColumnIndexOrThrow("id");
      final int _cursorIndexOfMDatasetName = _cursor.getColumnIndexOrThrow("datasetName");
      final DatasetWithFilledAutofillFields _result;
      if(_cursor.moveToFirst()) {
        final AutofillDataset _tmpAutofillDataset;
        if (! (_cursor.isNull(_cursorIndexOfMId) && _cursor.isNull(_cursorIndexOfMDatasetName))) {
          final String _tmpMId;
          _tmpMId = _cursor.getString(_cursorIndexOfMId);
          final String _tmpMDatasetName;
          _tmpMDatasetName = _cursor.getString(_cursorIndexOfMDatasetName);
          _tmpAutofillDataset = new AutofillDataset(_tmpMId,_tmpMDatasetName,null);
        }  else  {
          _tmpAutofillDataset = null;
        }
        _result = new DatasetWithFilledAutofillFields();
        if (!_cursor.isNull(_cursorIndexOfMId)) {
          final String _tmpKey = _cursor.getString(_cursorIndexOfMId);
          ArrayList<FilledAutofillField> _tmpCollection = _collectionFilledAutofillFields.get(_tmpKey);
          if(_tmpCollection == null) {
            _tmpCollection = new ArrayList<FilledAutofillField>();
            _collectionFilledAutofillFields.put(_tmpKey, _tmpCollection);
          }
          _result.filledAutofillFields = _tmpCollection;
        }
        _result.autofillDataset = _tmpAutofillDataset;
      } else {
        _result = null;
      }
      __fetchRelationshipFilledAutofillFieldAscomExampleAndroidAutofillServiceModelFilledAutofillField(_collectionFilledAutofillFields);
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public FilledAutofillField getFilledAutofillField(String datasetId, String fieldTypeName) {
    final String _sql = "SELECT * FROM FilledAutofillField WHERE FilledAutofillField.datasetId = (?) AND FilledAutofillField.fieldTypeName = (?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (datasetId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, datasetId);
    }
    _argIndex = 2;
    if (fieldTypeName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, fieldTypeName);
    }
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfMDatasetId = _cursor.getColumnIndexOrThrow("datasetId");
      final int _cursorIndexOfMTextValue = _cursor.getColumnIndexOrThrow("textValue");
      final int _cursorIndexOfMDateValue = _cursor.getColumnIndexOrThrow("dateValue");
      final int _cursorIndexOfMToggleValue = _cursor.getColumnIndexOrThrow("toggleValue");
      final int _cursorIndexOfMFieldTypeName = _cursor.getColumnIndexOrThrow("fieldTypeName");
      final FilledAutofillField _result;
      if(_cursor.moveToFirst()) {
        final String _tmpMDatasetId;
        _tmpMDatasetId = _cursor.getString(_cursorIndexOfMDatasetId);
        final String _tmpMTextValue;
        _tmpMTextValue = _cursor.getString(_cursorIndexOfMTextValue);
        final Long _tmpMDateValue;
        if (_cursor.isNull(_cursorIndexOfMDateValue)) {
          _tmpMDateValue = null;
        } else {
          _tmpMDateValue = _cursor.getLong(_cursorIndexOfMDateValue);
        }
        final Boolean _tmpMToggleValue;
        final Integer _tmp;
        if (_cursor.isNull(_cursorIndexOfMToggleValue)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getInt(_cursorIndexOfMToggleValue);
        }
        _tmpMToggleValue = _tmp == null ? null : _tmp != 0;
        final String _tmpMFieldTypeName;
        _tmpMFieldTypeName = _cursor.getString(_cursorIndexOfMFieldTypeName);
        _result = new FilledAutofillField(_tmpMDatasetId,_tmpMFieldTypeName,_tmpMTextValue,_tmpMDateValue,_tmpMToggleValue);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public FieldType getFieldType(String fieldTypeName) {
    final String _sql = "SELECT * FROM FieldType WHERE FieldType.typeName = (?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (fieldTypeName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, fieldTypeName);
    }
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfMTypeName = _cursor.getColumnIndexOrThrow("typeName");
      final int _cursorIndexOfMAutofillTypes = _cursor.getColumnIndexOrThrow("autofillTypes");
      final int _cursorIndexOfMSaveInfo = _cursor.getColumnIndexOrThrow("saveInfo");
      final int _cursorIndexOfMPartition = _cursor.getColumnIndexOrThrow("partition");
      final int _cursorIndexOfStrictExampleSet = _cursor.getColumnIndexOrThrow("strictExampleSet");
      final int _cursorIndexOfTextTemplate = _cursor.getColumnIndexOrThrow("textTemplate");
      final int _cursorIndexOfDateTemplate = _cursor.getColumnIndexOrThrow("dateTemplate");
      final FieldType _result;
      if(_cursor.moveToFirst()) {
        final String _tmpMTypeName;
        _tmpMTypeName = _cursor.getString(_cursorIndexOfMTypeName);
        final Converters.IntList _tmpMAutofillTypes;
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfMAutofillTypes);
        _tmpMAutofillTypes = Converters.storedStringToIntList(_tmp);
        final Integer _tmpMSaveInfo;
        if (_cursor.isNull(_cursorIndexOfMSaveInfo)) {
          _tmpMSaveInfo = null;
        } else {
          _tmpMSaveInfo = _cursor.getInt(_cursorIndexOfMSaveInfo);
        }
        final Integer _tmpMPartition;
        if (_cursor.isNull(_cursorIndexOfMPartition)) {
          _tmpMPartition = null;
        } else {
          _tmpMPartition = _cursor.getInt(_cursorIndexOfMPartition);
        }
        final FakeData _tmpMFakeData;
        final Converters.StringList _tmpStrictExampleSet;
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfStrictExampleSet);
        _tmpStrictExampleSet = Converters.storedStringToStringList(_tmp_1);
        final String _tmpTextTemplate;
        _tmpTextTemplate = _cursor.getString(_cursorIndexOfTextTemplate);
        final String _tmpDateTemplate;
        _tmpDateTemplate = _cursor.getString(_cursorIndexOfDateTemplate);
        _tmpMFakeData = new FakeData(_tmpStrictExampleSet,_tmpTextTemplate,_tmpDateTemplate);
        _result = new FieldType(_tmpMTypeName,_tmpMAutofillTypes,_tmpMSaveInfo,_tmpMPartition,_tmpMFakeData);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  private void __fetchRelationshipFilledAutofillFieldAscomExampleAndroidAutofillServiceModelFilledAutofillField(final ArrayMap<String, ArrayList<FilledAutofillField>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT datasetId,textValue,dateValue,toggleValue,fieldTypeName FROM `FilledAutofillField` WHERE datasetId IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : __mapKeySet) {
      if (_item == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindString(_argIndex, _item);
      }
      _argIndex ++;
    }
    final Cursor _cursor = __db.query(_stmt);
    try {
      final int _itemKeyIndex = _cursor.getColumnIndex("datasetId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfMDatasetId = _cursor.getColumnIndexOrThrow("datasetId");
      final int _cursorIndexOfMTextValue = _cursor.getColumnIndexOrThrow("textValue");
      final int _cursorIndexOfMDateValue = _cursor.getColumnIndexOrThrow("dateValue");
      final int _cursorIndexOfMToggleValue = _cursor.getColumnIndexOrThrow("toggleValue");
      final int _cursorIndexOfMFieldTypeName = _cursor.getColumnIndexOrThrow("fieldTypeName");
      while(_cursor.moveToNext()) {
        if (!_cursor.isNull(_itemKeyIndex)) {
          final String _tmpKey = _cursor.getString(_itemKeyIndex);
          ArrayList<FilledAutofillField> _tmpCollection = _map.get(_tmpKey);
          if (_tmpCollection != null) {
            final FilledAutofillField _item_1;
            final String _tmpMDatasetId;
            _tmpMDatasetId = _cursor.getString(_cursorIndexOfMDatasetId);
            final String _tmpMTextValue;
            _tmpMTextValue = _cursor.getString(_cursorIndexOfMTextValue);
            final Long _tmpMDateValue;
            if (_cursor.isNull(_cursorIndexOfMDateValue)) {
              _tmpMDateValue = null;
            } else {
              _tmpMDateValue = _cursor.getLong(_cursorIndexOfMDateValue);
            }
            final Boolean _tmpMToggleValue;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfMToggleValue)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfMToggleValue);
            }
            _tmpMToggleValue = _tmp == null ? null : _tmp != 0;
            final String _tmpMFieldTypeName;
            _tmpMFieldTypeName = _cursor.getString(_cursorIndexOfMFieldTypeName);
            _item_1 = new FilledAutofillField(_tmpMDatasetId,_tmpMFieldTypeName,_tmpMTextValue,_tmpMDateValue,_tmpMToggleValue);
            _tmpCollection.add(_item_1);
          }
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshipAutofillHintAscomExampleAndroidAutofillServiceModelAutofillHint(final ArrayMap<String, ArrayList<AutofillHint>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT autofillHint,fieldTypeName FROM `AutofillHint` WHERE fieldTypeName IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : __mapKeySet) {
      if (_item == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindString(_argIndex, _item);
      }
      _argIndex ++;
    }
    final Cursor _cursor = __db.query(_stmt);
    try {
      final int _itemKeyIndex = _cursor.getColumnIndex("fieldTypeName");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfMAutofillHint = _cursor.getColumnIndexOrThrow("autofillHint");
      final int _cursorIndexOfMFieldTypeName = _cursor.getColumnIndexOrThrow("fieldTypeName");
      while(_cursor.moveToNext()) {
        if (!_cursor.isNull(_itemKeyIndex)) {
          final String _tmpKey = _cursor.getString(_itemKeyIndex);
          ArrayList<AutofillHint> _tmpCollection = _map.get(_tmpKey);
          if (_tmpCollection != null) {
            final AutofillHint _item_1;
            final String _tmpMAutofillHint;
            _tmpMAutofillHint = _cursor.getString(_cursorIndexOfMAutofillHint);
            final String _tmpMFieldTypeName;
            _tmpMFieldTypeName = _cursor.getString(_cursorIndexOfMFieldTypeName);
            _item_1 = new AutofillHint(_tmpMAutofillHint,_tmpMFieldTypeName);
            _tmpCollection.add(_item_1);
          }
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshipResourceIdHeuristicAscomExampleAndroidAutofillServiceModelResourceIdHeuristic(final ArrayMap<String, ArrayList<ResourceIdHeuristic>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT resourceIdHeuristic,packageName,fieldTypeName FROM `ResourceIdHeuristic` WHERE fieldTypeName IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : __mapKeySet) {
      if (_item == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindString(_argIndex, _item);
      }
      _argIndex ++;
    }
    final Cursor _cursor = __db.query(_stmt);
    try {
      final int _itemKeyIndex = _cursor.getColumnIndex("fieldTypeName");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfMResourceIdHeuristic = _cursor.getColumnIndexOrThrow("resourceIdHeuristic");
      final int _cursorIndexOfMPackageName = _cursor.getColumnIndexOrThrow("packageName");
      final int _cursorIndexOfMFieldTypeName = _cursor.getColumnIndexOrThrow("fieldTypeName");
      while(_cursor.moveToNext()) {
        if (!_cursor.isNull(_itemKeyIndex)) {
          final String _tmpKey = _cursor.getString(_itemKeyIndex);
          ArrayList<ResourceIdHeuristic> _tmpCollection = _map.get(_tmpKey);
          if (_tmpCollection != null) {
            final ResourceIdHeuristic _item_1;
            final String _tmpMResourceIdHeuristic;
            _tmpMResourceIdHeuristic = _cursor.getString(_cursorIndexOfMResourceIdHeuristic);
            final String _tmpMPackageName;
            _tmpMPackageName = _cursor.getString(_cursorIndexOfMPackageName);
            final String _tmpMFieldTypeName;
            _tmpMFieldTypeName = _cursor.getString(_cursorIndexOfMFieldTypeName);
            _item_1 = new ResourceIdHeuristic(_tmpMResourceIdHeuristic,_tmpMFieldTypeName,_tmpMPackageName);
            _tmpCollection.add(_item_1);
          }
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
