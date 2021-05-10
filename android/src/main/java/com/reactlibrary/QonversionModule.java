package com.reactlibrary;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.qonversion.android.sdk.AttributionSource;
import com.qonversion.android.sdk.QUserProperties;
import com.qonversion.android.sdk.Qonversion;
import com.qonversion.android.sdk.QonversionEligibilityCallback;
import com.qonversion.android.sdk.QonversionError;
import com.qonversion.android.sdk.QonversionExperimentsCallback;
import com.qonversion.android.sdk.QonversionLaunchCallback;
import com.qonversion.android.sdk.QonversionOfferingsCallback;
import com.qonversion.android.sdk.QonversionPermissionsCallback;
import com.qonversion.android.sdk.QonversionProductsCallback;
import com.qonversion.android.sdk.dto.QLaunchResult;
import com.qonversion.android.sdk.dto.experiments.QExperimentInfo;
import com.qonversion.android.sdk.dto.offerings.QOfferings;
import com.qonversion.android.sdk.dto.QPermission;
import com.qonversion.android.sdk.dto.products.QProduct;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.qonversion.android.sdk.dto.eligibility.QEligibility;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class QonversionModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static final HashMap<Integer, QUserProperties> userPropertiesMap = new HashMap<Integer, QUserProperties>() {{
        put(0, QUserProperties.Email);
        put(1, QUserProperties.Name);
        put(2, QUserProperties.AppsFlyerUserId);
        put(3, QUserProperties.AdjustAdId);
        put(4, QUserProperties.KochavaDeviceId);
        put(5, QUserProperties.CustomUserId);
        put(6, QUserProperties.FacebookAttribution);
    }};

    public QonversionModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNQonversion";
    }

    @ReactMethod
    public void storeSDKInfo(String sourceKey, String source, String sdkVersionKey, String sdkVersion) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity().getApplication()).edit();
        editor.putString(sdkVersionKey, sdkVersion);
        editor.putString(sourceKey, source);
        editor.apply();
    }

    @ReactMethod
    public void launchWithKey(String key, Boolean observeMode, final Promise promise) {
        Qonversion.launch(getCurrentActivity().getApplication(), key, observeMode, new QonversionLaunchCallback()
        {
            @Override
            public void onSuccess(@NotNull QLaunchResult qLaunchResult) {
                WritableMap result = EntitiesConverter.mapLaunchResult(qLaunchResult);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError qonversionError) {
                promise.reject(qonversionError.getCode().toString(), qonversionError.getDescription());
            }
        });
    }

    @ReactMethod
    public void purchase(String productId, final Promise promise) {
        Qonversion.purchase(getCurrentActivity(), productId, new QonversionPermissionsCallback() {
            @Override
            public void onSuccess(@NotNull Map<String, QPermission> map) {
                WritableMap result = EntitiesConverter.mapPermissions(map);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError qonversionError) {
                promise.reject(qonversionError.getCode().toString(), qonversionError.getDescription());
            }
        });
    }

    @ReactMethod
    public void updatePurchaseWithProrationMode(String productId, String oldProductId, Integer prorationMode, final Promise promise) {
        Qonversion.updatePurchase(getCurrentActivity(), productId, oldProductId, prorationMode, new QonversionPermissionsCallback() {
            @Override
            public void onSuccess(@NotNull Map<String, QPermission> map) {
                WritableMap result = EntitiesConverter.mapPermissions(map);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError qonversionError) {
                promise.reject(qonversionError.getCode().toString(), qonversionError.getDescription());
            }
        });
    }

    @ReactMethod
    public void updatePurchase(String productId, String oldProductId, final Promise promise) {
        updatePurchaseWithProrationMode(productId, oldProductId, null, promise);
    }

    @ReactMethod
    public void setProperty(Integer key, String value) {
        QUserProperties property = userPropertiesMap.get(key);

        if (property != null) {
            Qonversion.setProperty(property, value);
        }
    }

    @ReactMethod
    public void setUserProperty(String key, String value) {
        Qonversion.setUserProperty(key, value);
    }

    @ReactMethod
    public void setUserId(String value) {
        Qonversion.setUserID(value);
    }

    @ReactMethod
    public void addAttributionData(ReadableMap map, Integer provider) {
        AttributionSource source = null;
        switch (provider) {
            case 0:
                source = AttributionSource.AppsFlyer;
                break;
            case 1:
                source = AttributionSource.Branch;
                break;
            case 2:
                source = AttributionSource.Adjust;
                break;
        }

        if (source == null) {
            return;
        }

        try {
            HashMap attributesHashMap = EntitiesConverter.convertReadableMapToHashMap(map);
            Qonversion.attribution(attributesHashMap, source);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void checkPermissions(final Promise promise) {
        Qonversion.checkPermissions(new QonversionPermissionsCallback() {
            @Override
            public void onSuccess(@NotNull Map<String, QPermission> map) {
                WritableMap result = EntitiesConverter.mapPermissions(map);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError qonversionError) {
                promise.reject(qonversionError.getCode().toString(), qonversionError.getDescription());
            }
        });
    }

    @ReactMethod
    public void products(final Promise promise) {
        Qonversion.products(new QonversionProductsCallback() {
            @Override
            public void onSuccess(@NotNull Map<String, QProduct> map) {
                WritableMap result = EntitiesConverter.mapProducts(map);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError qonversionError) {
                promise.reject(qonversionError.getCode().toString(), qonversionError.getDescription());
            }
        });
    }

    @ReactMethod
    public void offerings(final Promise promise) {
        Qonversion.offerings(new QonversionOfferingsCallback() {
            @Override
            public void onSuccess(@NotNull QOfferings offerings) {
                WritableMap result = EntitiesConverter.mapOfferings(offerings);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError error) {
                promise.reject(error.getCode().toString(), error.getDescription());
            }
        });
    }

    @ReactMethod
    public void checkTrialIntroEligibilityForProductIds(ReadableArray ids, final Promise promise) {
        List<String> result = Arrays.asList(ids.toArrayList().toArray(new String[ids.size()]));
        Qonversion.checkTrialIntroEligibilityForProductIds(result, new QonversionEligibilityCallback() {
            @Override
            public void onSuccess(@NotNull Map<String, QEligibility> map) {
                WritableArray result = EntitiesConverter.mapEligibility(map);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError qonversionError) {
                promise.reject(qonversionError.getCode().toString(), qonversionError.getDescription());
            }
        });
    }

    @ReactMethod
    public void experiments(final Promise promise) {
        Qonversion.experiments(new QonversionExperimentsCallback() {
            @Override
            public void onSuccess(@NotNull Map<String, QExperimentInfo> map) {
                WritableArray result = EntitiesConverter.mapExperiments(map);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError qonversionError) {
                promise.reject(qonversionError.getCode().toString(), qonversionError.getDescription());
            }
        });
    }

    @ReactMethod
    public void restore(final Promise promise) {
        Qonversion.restore(new QonversionPermissionsCallback() {
            @Override
            public void onSuccess(@NotNull Map<String, QPermission> map) {
                WritableMap result = EntitiesConverter.mapPermissions(map);
                promise.resolve(result);
            }

            @Override
            public void onError(@NotNull QonversionError qonversionError) {
                promise.reject(qonversionError.getCode().toString(), qonversionError.getDescription());
            }
        });
    }

    @ReactMethod
    public void syncPurchases() {
        Qonversion.syncPurchases();
    }

    @ReactMethod
    public void setDebugMode() {
        Qonversion.setDebugMode();
    }
}
