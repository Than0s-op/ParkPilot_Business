package com.application.parkpilotreg.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.parkpilotreg.FreeSpot
import com.application.parkpilotreg.R
import com.application.parkpilotreg.adapter.FreeSpotListAdapter
import com.application.parkpilotreg.databinding.FreeSpotListBinding
import com.application.parkpilotreg.viewModel.FreeSpotListViewModel

class FreeSpotList : AppCompatActivity() {
    private lateinit var binding: FreeSpotListBinding
    private lateinit var viewModel: FreeSpotListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FreeSpotListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = FreeSpotListViewModel()
        binding.freeSpotList.layoutManager = LinearLayoutManager(this)
        viewModel.getFreeSpotList({
            hideShimmer()
        })
        viewModel.freeSpotList.observe(this) {
            loadFreeSpotList(it)
        }

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = viewModel.freeSpotList.value?.filter {
                    it.landMark.contains(newText ?: "")
                } ?: emptyList()
                loadFreeSpotList(filteredList)
                return true
            }
        })
    }

    private fun loadFreeSpotList(list: List<FreeSpot>) {
        binding.freeSpotList.adapter = FreeSpotListAdapter(
            context = this,
            layout = R.layout.free_spot_list_item,
            freeSpotList = list,
            onDeleteClick = { documentId ->
                viewModel.removeSpot(documentId,
                    onSuccess = {
                        Toast.makeText(this, "removed Successfully", Toast.LENGTH_SHORT).show()
                        showShimmer()
                        viewModel.getFreeSpotList({
                            hideShimmer()
                        })
                    },
                    onFailure = {
                        Toast.makeText(this, "Failed to remove", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    private fun showShimmer() {
        binding.shimmerScrollView.visibility = View.VISIBLE
        binding.freeSpotList.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerScrollView.visibility = View.GONE
        binding.freeSpotList.visibility = View.VISIBLE
    }
}
