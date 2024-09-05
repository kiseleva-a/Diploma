package ru.netology.nework.fragment.secondary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.user_location.UserLocationLayer
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapBinding
import ru.netology.nework.dto.Coords
import ru.netology.nework.utils.BooleanArg
import ru.netology.nework.viewmodel.UsersAndMapViewModel

class MapFragment : Fragment() {
    private var locationPermission = false
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                locationPermission = true
                cameraToUserPosition()
            } else {
                showPermissionSnackbar()
            }
        }

    private val viewModel: UsersAndMapViewModel by activityViewModels()

    private var userLocation = Point(0.0, 0.0)
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var nowTarget: Point

    private val binding: FragmentMapBinding by viewBinding(createMethod = CreateMethod.INFLATE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        checkMapPermission()
        MapKitFactory.initialize(requireContext())

        binding.gettingPlaceContainer.isVisible = arguments?.editingArg == true
        setUpMap()

        return binding.root
    }

    fun moveMap(target: Point, zoom: Float = 15f, azimuth: Float = 0f, tilt: Float = 0f) {
        binding.mapView.map.move(
            CameraPosition(target, zoom, azimuth, tilt),
            Animation(Animation.Type.SMOOTH, 3f),
            null
        )
    }

    private fun setUpMap() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(binding.mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = false

        val lat: Double = viewModel.coords?.lat?.toDouble() ?: 0.0
        val long: Double = viewModel.coords?.long?.toDouble() ?: 0.0
        if (lat != 0.0 && long != 0.0) {
            val target = Point(lat, long)
            moveMap(target)
        }

        if (arguments?.editingArg == true)
            findingCoords()
    }

    private fun findingCoords() {
        binding.apply {
            findMyLocationFab.setOnClickListener {
                if (locationPermission) {
                    cameraToUserPosition()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            mapView.map.apply {
                addCameraListener(cameraListener)
            }

            addPlaceFab.setOnClickListener {
                viewModel.coords =
                    Coords(nowTarget.latitude.toString().take(9), nowTarget.longitude.toString().take(9))
                findNavController().navigateUp()
            }
        }
    }

    val cameraListener: CameraListener =
        CameraListener { _, cameraPosition, _, _ ->
            binding.coordinates.text = "${cameraPosition.target.latitude.toString().take(9)} | ${
                cameraPosition.target.longitude.toString().take(9)
            }"
            nowTarget = cameraPosition.target
        }

    private fun cameraToUserPosition(zoom: Float = 16f) {
        if (userLocationLayer.cameraPosition() != null) {
            userLocation = userLocationLayer.cameraPosition()!!.target
            moveMap(userLocation, zoom)
        } else {
            Snackbar.make(
                binding.mapView,
                getString(R.string.no_user_location_error),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    cameraToUserPosition()
                }
                .show()
        }
    }

    private fun checkMapPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                locationPermission = true
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionSnackbar()
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun showPermissionSnackbar() {
        Snackbar.make(binding.mapView, getString(R.string.need_geolocation), Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.permission)) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .show()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    companion object {
        var Bundle.editingArg by BooleanArg
    }
}