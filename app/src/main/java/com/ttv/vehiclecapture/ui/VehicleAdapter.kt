package com.ttv.vehiclecapture.ui

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ttv.vehiclecapture.databinding.ItemVehicleBinding
import com.ttv.vehiclecapture.model.Vehicle

class VehicleAdapter(
    private var vehicles: List<Vehicle>,
    private val onDeleteClick: (Vehicle) -> Unit,
    private val onEditClick: (Vehicle) -> Unit
): RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    class VehicleViewHolder(
        private val binding: ItemVehicleBinding,
        private val onDeleteClick: (Vehicle) -> Unit,
        private val onEditClick: (Vehicle) -> Unit

    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: Vehicle){
            binding.vehicle = vehicle

            if(vehicle.photoUri != null){
                val imageBytes = Base64.decode(vehicle.photoUri, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.vehiclePhotoImageView.setImageBitmap(bitmap)
            }else{
                binding.vehiclePhotoImageView.setImageResource(android.R.drawable.ic_menu_camera)
            }

            binding.deleteVehicleButton.setOnClickListener {
                onDeleteClick(vehicle)
            }

            binding.editVehicleButton.setOnClickListener {
                onEditClick(vehicle)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemVehicleBinding.inflate(inflater, parent, false)
        return VehicleViewHolder(binding, onDeleteClick, onEditClick)
    }

    override fun onBindViewHolder(
        holder: VehicleViewHolder,
        position: Int
    ) {
       holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int {
        return vehicles.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newVehicles: List<Vehicle>){
        vehicles = newVehicles
        notifyDataSetChanged()
    }

}