package com.br.myfavoritehero.features.listCharacter

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.br.myfavoritehero.R
import com.br.myfavoritehero.data.interfaces.HeroEventListener
import com.br.myfavoritehero.data.models.Hero
import com.br.myfavoritehero.data.models.ViewStateModel
import com.br.myfavoritehero.features.heroDetails.DetailHeroActivity
import com.br.myfavoritehero.features.listener.EndlessScrollListener
import com.br.myfavoritehero.util.Constants.HERO
import kotlinx.android.synthetic.main.activity_list_heroes.*
import timber.log.Timber

class ListHeroesActivity : AppCompatActivity(), HeroEventListener {

    private lateinit var heroAdapter: HeroAdapter

    val listCharacterViewModel: ListHeroesViewModel = ListHeroesViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_heroes)
        initObservable()
    }

    fun initObservable(){
        listCharacterViewModel.getHeroes().observe(this, Observer{ stateModel ->

            when(stateModel.status){
                ViewStateModel.Status.ERROR -> {
                    Timber.d("ERROR: ${stateModel.errors.toString()}")
                    progressBarList.visibility = View.GONE
                }
                ViewStateModel.Status.SUCCESS -> {
                    progressBarList.visibility = View.GONE

                    listHeroes.setHasFixedSize(true)
                    val layoutManager = LinearLayoutManager(this)
                    listHeroes.layoutManager = layoutManager
                    heroAdapter = HeroAdapter(stateModel.model!!, this)
                    listHeroes.adapter = heroAdapter
                    listHeroes.addOnScrollListener(object : EndlessScrollListener(layoutManager){
                        override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                            listCharacterViewModel.loadMore(totalItemsCount)
                        }
                    })

                }
                ViewStateModel.Status.LOADING -> {
                    progressBarList.visibility = View.VISIBLE
                    Timber.d("LOADING: ... ")
                }
            }
        })

        listCharacterViewModel.getMore().observe(this, Observer { stateModel ->
            when(stateModel.status){
                ViewStateModel.Status.ERROR -> {
                    Timber.d("ERROR: ${stateModel.errors.toString()}")
                    tinyProgressBar.visibility = View.GONE
                }
                ViewStateModel.Status.SUCCESS -> {
                    tinyProgressBar.visibility = View.GONE
                    heroAdapter.updateUI(stateModel.model!!)
                }
                ViewStateModel.Status.LOADING -> {
                    tinyProgressBar.visibility = View.VISIBLE
                    Timber.d("LOADING: ... ")
                }
            }
        })

        listCharacterViewModel.loadHeroes()

    }

    override fun onHeroClicked(hero: Hero) {
        val i = Intent(this, DetailHeroActivity::class.java)
        i.putExtra(HERO, hero)
        startActivity(i)
    }

}