package com.application.parkpilotreg.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.parkpilotreg.R
import com.application.parkpilotreg.adapter.FreeSpotListAdapter
import com.application.parkpilotreg.databinding.FreeSpotListBinding
import com.application.parkpilotreg.viewModel.FreeSpotListViewModel

class FreeSpotList : AppCompatActivity() {
    private lateinit var binding: FreeSpotListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FreeSpotListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel = FreeSpotListViewModel()
        binding.freeSpotList.layoutManager = LinearLayoutManager(this)

        viewModel.freeSpotList.observe(this) {
            binding.freeSpotList.adapter = FreeSpotListAdapter(
                context = this,
                layout = R.layout.free_spot_list_item,
                freeSpotList = it,
                onDeleteClick = { documentId ->
                    viewModel.removeSpot(documentId,
                        onSuccess = {
                            Toast.makeText(this, "removed Successfully", Toast.LENGTH_SHORT).show()
                            viewModel.getFreeSpotList()
                        },
                        onFailure = {
                            Toast.makeText(this, "Failed to remove", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}
