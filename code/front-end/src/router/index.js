import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hideLayout: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据概览', icon: 'DataAnalysis' }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/customer/index.vue'),
        meta: { title: '客户管理', icon: 'User' }
      },
      {
        path: 'customer/detail/:id',
        name: 'CustomerDetail',
        component: () => import('@/views/customer/detail.vue'),
        meta: { title: '客户详情', hidden: true }
      },
      {
        path: 'project',
        name: 'Project',
        component: () => import('@/views/project/index.vue'),
        meta: { title: '项目管理', icon: 'Goods' }
      },
      {
        path: 'package',
        name: 'Package',
        component: () => import('@/views/project/package.vue'),
        meta: { title: '套餐管理', icon: 'Box' }
      },
      {
        path: 'appointment',
        name: 'Appointment',
        component: () => import('@/views/appointment/index.vue'),
        meta: { title: '预约管理', icon: 'Calendar' }
      },
      {
        path: 'consume',
        name: 'Consume',
        component: () => import('@/views/consume/index.vue'),
        meta: { title: '消费记录', icon: 'List' }
      },
      {
        path: 'recharge',
        name: 'Recharge',
        component: () => import('@/views/recharge/index.vue'),
        meta: { title: '客户充值', icon: 'Wallet' }
      },
      {
        path: 'purchase',
        name: 'Purchase',
        component: () => import('@/views/purchase/index.vue'),
        meta: { title: '套餐购买', icon: 'ShoppingCart' }
      },
      {
        path: 'member-card',
        name: 'MemberCard',
        component: () => import('@/views/member-card/index.vue'),
        meta: { title: '会员卡管理', icon: 'Postcard' }
      },
      {
        path: 'member-level',
        name: 'MemberLevel',
        component: () => import('@/views/member-level/index.vue'),
        meta: { title: '会员等级', icon: 'Medal' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '产品管理', icon: 'ShoppingBag' }
      },
      {
        path: 'stock-in',
        name: 'StockIn',
        component: () => import('@/views/stock/in.vue'),
        meta: { title: '入库管理', icon: 'Download' }
      },
      {
        path: 'stock-out',
        name: 'StockOut',
        component: () => import('@/views/stock/out.vue'),
        meta: { title: '出库管理', icon: 'Upload' }
      },
      {
        path: 'customer-care',
        name: 'CustomerCare',
        component: () => import('@/views/care/index.vue'),
        meta: { title: '客户维系', icon: 'ChatDotRound' }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/message/index.vue'),
        meta: { title: '消息中心', icon: 'Bell' }
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/analysis/index.vue'),
        meta: { title: '数据分析', icon: 'TrendCharts' }
      },
      {
        path: 'analysis/revenue-detail',
        name: 'RevenueDetail',
        component: () => import('@/views/analysis/revenue-detail.vue'),
        meta: { title: '营收趋势明细', hidden: true }
      },
      {
        path: 'analysis/project-detail',
        name: 'ProjectDetail',
        component: () => import('@/views/analysis/project-detail.vue'),
        meta: { title: '项目销量明细', hidden: true }
      },
      {
        path: 'analysis/source-detail',
        name: 'SourceDetail',
        component: () => import('@/views/analysis/source-detail.vue'),
        meta: { title: '客户来源明细', hidden: true }
      },
      {
        path: 'analysis/staff-detail',
        name: 'StaffDetail',
        component: () => import('@/views/analysis/staff-detail.vue'),
        meta: { title: '员工业绩明细', hidden: true }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role.vue'),
        meta: { title: '角色管理', icon: 'Avatar' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict.vue'),
        meta: { title: '字典管理', icon: 'Collection' }
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/log.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hideLayout: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - BCRM` : 'BCRM'
  
  const userStore = useUserStore()
  const token = userStore.token
  
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
