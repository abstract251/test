import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { restoreAuthSession } from '@/bootstrap/auth'
import { installElementPlus } from '@/plugins/element-plus'
import '@/assets/styles/theme.css'
import '@/assets/styles/base.css'

async function bootstrap() {
  await restoreAuthSession()

  const app = createApp(App)
  installElementPlus(app)
  app.use(router).mount('#app')
}

bootstrap()
