package ma.ensa.graphqltp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import ma.ensa.graphqltp.adapter.ComptesAdapter
import ma.ensa.graphqltp.data.MainViewModel
import ma.ensa.graphqltp.type.TypeCompte

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var totalCountText: TextView
    private lateinit var totalSumText: TextView
    private lateinit var averageText: TextView
    private lateinit var statsCard: View

    private val viewModel: MainViewModel by viewModels()
    private val comptesAdapter = ComptesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        setupAddButton()
        observeViewModel()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.comptesRecyclerView)
        addButton = findViewById(R.id.addCompteButton)
        totalCountText = findViewById(R.id.totalCountText)
        totalSumText = findViewById(R.id.totalSumText)
        averageText = findViewById(R.id.averageText)
        statsCard = findViewById(R.id.statsCard)
    }

    private fun setupRecyclerView() {
        recyclerView.apply {
            adapter = comptesAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupAddButton() {
        addButton.setOnClickListener {
            showAddCompteDialog()
        }
    }

    private fun observeViewModel() {
        // Observe the list of comptes
        viewModel.comptesState.observe(this) { state ->
            when (state) {
                is MainViewModel.UIState.Loading -> {
                    // Show loading UI for comptes
                }
                is MainViewModel.UIState.Success -> {
                    comptesAdapter.updateList(state.data)
                }
                is MainViewModel.UIState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observe the total solde statistics
        viewModel.totalSoldeState.observe(this) { state ->
            when (state) {
                is MainViewModel.UIState.Loading -> {
                    statsCard.visibility = View.INVISIBLE
                }
                is MainViewModel.UIState.Success -> {
                    statsCard.visibility = View.VISIBLE
                    // Update the stats
                    totalCountText.text = "Total des comptes: ${state.data?.count}"
                    totalSumText.text = "Total des soldes: ${state.data?.sum} €"
                    averageText.text = "balance moyenne: ${state.data?.average} €"
                }
                is MainViewModel.UIState.Error -> {
                    statsCard.visibility = View.INVISIBLE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showAddCompteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_compte, null)
        val soldeInput = dialogView.findViewById<TextInputEditText>(R.id.soldeInput)
        val typeGroup = dialogView.findViewById<RadioGroup>(R.id.typeGroup)

        MaterialAlertDialogBuilder(this)
            .setTitle("Ajouter un nouveau compte")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val solde = soldeInput.text.toString().toDoubleOrNull()
                val selectedId = typeGroup.checkedRadioButtonId
                val typeRadioButton = dialogView.findViewById<RadioButton>(selectedId)
                val type = when (typeRadioButton.text.toString().uppercase()) {
                    "COURANT" -> TypeCompte.COURANT
                    "EPARGNE" -> TypeCompte.EPARGNE
                    else -> TypeCompte.COURANT
                }

                if (solde != null) {
                    viewModel.saveCompte(solde, type)
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
