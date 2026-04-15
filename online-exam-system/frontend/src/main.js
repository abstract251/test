import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import { restoreAuthSession } from '@/bootstrap/auth'
import '@/assets/styles/theme.css'
import '@/assets/styles/base.css'

async function bootstrap() {
  await restoreAuthSession()

  createApp(App)
    .use(ElementPlus)
    .use(router)
    .mount('#app')
}

bootstrap()
