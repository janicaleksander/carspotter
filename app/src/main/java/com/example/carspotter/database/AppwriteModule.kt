package com.example.carspotter.database
import com.example.carspotter.BuildConfig
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.TablesDB
import io.github.cdimascio.dotenv.dotenv
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppwriteModule {

    @Provides
    @Singleton
    fun provideAppwriteClient(@ApplicationContext context: Context): Client {
        return Client(context)
            .setEndpoint(BuildConfig.APPWRITE_PUBLIC_ENDPOINT)
            .setProject(BuildConfig.APPWRITE_PROJECT_ID)
    }

    @Provides
    @Singleton
    fun provideAppwriteAccount(client: Client): Account {
        return Account(client)
    }

    @Provides
    @Singleton
    fun provideAppwriteDatabases(client: Client): Databases {
        return Databases(client)
    }
    @Provides
    @Singleton
    fun provideAppwriteTables(client: Client): TablesDB {
        return TablesDB(client)
    }


    @Provides
    @Singleton
    fun provideAppwriteStorage(client: Client): Storage {
        return Storage(client)
    }
}
