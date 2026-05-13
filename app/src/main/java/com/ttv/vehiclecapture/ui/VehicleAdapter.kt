package com.ttv.vehiclecapture.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.ttv.vehiclecapture.databinding.ItemVehicleBinding
import com.ttv.vehiclecapture.model.Vehicle

class VehicleAdapter(
    private var vehicles: List<Vehicle>,
    private val onDeleteClick: (Vehicle) -> Unit,
    private val onEditClick: (Vehicle) -> Unit,
    private val onVehicleClick: (Vehicle) -> Unit
): RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    class VehicleViewHolder(
        private val binding: ItemVehicleBinding,
        private val onDeleteClick: (Vehicle) -> Unit,
        private val onEditClick: (Vehicle) -> Unit,
        private val onVehicleClick: (Vehicle) -> Unit

    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: Vehicle){
            binding.vehicle = vehicle

            if(vehicle.photoUri != null){
                binding.vehiclePhotoImageView.setImageURI(vehicle.photoUri.toUri())
            }else{
                binding.vehiclePhotoImageView.setImageResource(android.R.drawable.ic_menu_camera)
            }

            binding.deleteVehicleButton.setOnClickListener {
                onDeleteClick(vehicle)
            }

            binding.editVehicleButton.setOnClickListener {
                onEditClick(vehicle)
            }

            binding.root.setOnClickListener{
                onVehicleClick(vehicle)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemVehicleBinding.inflate(inflater, parent, false)
        return VehicleViewHolder(
            binding,
            onDeleteClick,
            onEditClick,
            onVehicleClick)
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