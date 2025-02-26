package com.example.bitequest.ui.presentation.screens.client

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks

class LocationUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val truckId = inputData.getString("truckId") ?: return Result.failure()

        // ✅ التحقق من الصلاحيات قبل طلب الموقع
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("🔴 الصلاحية غير متوفرة، يجب طلبها قبل الوصول للموقع")
            return Result.failure()
        }

        try {
            val locationTask = fusedLocationClient.lastLocation
            val location = Tasks.await(locationTask)

            if (location != null) {
                db.collection("foodTrucks").document(truckId)
                    .update("latitude", location.latitude, "longitude", location.longitude)
                    .addOnSuccessListener {
                        println("✅ تم تحديث الموقع بنجاح")
                    }
                    .addOnFailureListener { e ->
                        println("❌ فشل في تحديث الموقع: $e")
                    }
            } else {
                println("⚠️ الموقع غير متاح حاليًا")
            }
        } catch (e: Exception) {
            println("❌ خطأ أثناء جلب الموقع: $e")
            return Result.retry()
        }

        return Result.success()
    }
}
