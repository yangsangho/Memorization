package kr.yangbob.memorization

import android.content.Context
import androidx.room.Room
import kr.yangbob.memorization.db.MemDatabase
import kr.yangbob.memorization.model.MemRepository
import kr.yangbob.memorization.viewmodel.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(get(), get())
    }
    viewModel {
        CreateViewModel(get())
    }
    viewModel {
        TestViewModel(get())
    }
    viewModel {
        ResultViewModel(get(), get())
    }
    viewModel {
        EntireViewModel(get(), get())
    }
    viewModel {
        QstViewModel(get())
    }
    viewModel {
        CalendarViewModel(get())
    }
    single { MemRepository( get(), get() ) }
}

val dbModule = module {
    single {
        androidContext().getSharedPreferences("SETTING", Context.MODE_PRIVATE)
    }
    single {
        Room.databaseBuilder(androidContext(), MemDatabase::class.java, "BeomS_Memo")
                .fallbackToDestructiveMigration().build()
    }

}

val testDbModule = module {
    single {
        androidContext().getSharedPreferences("TEST_SETTING", Context.MODE_PRIVATE)
    }
    single{
        Room.inMemoryDatabaseBuilder(androidContext(), MemDatabase::class.java).build()
    }
}