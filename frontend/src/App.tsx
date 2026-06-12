import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from './components/AppLayout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { DashboardPage } from './pages/DashboardPage';
import { DisponibilidadePage } from './pages/DisponibilidadePage';
import { LoginPage } from './pages/LoginPage';
import { UsuariosPage } from './pages/UsuariosPage';
import { EquipamentosPage } from './pages/admin/EquipamentosPage';
import { RegrasDisponibilidadePage } from './pages/admin/RegrasDisponibilidadePage';
import { IaExecucoesPage } from './pages/admin/IaExecucoesPage';
import { LayoutsPage, PosicoesPage } from './pages/admin/LayoutsPosicoesPages';
import { EquipeDetailPage } from './pages/equipes/EquipeDetailPage';
import { EquipeFormPage, EquipesListPage } from './pages/equipes/EquipesPages';
import { MinhaEquipeDetailPage } from './pages/equipes/MinhaEquipeDetailPage';
import { MinhasEquipesPage } from './pages/equipes/MinhasEquipesPage';
import { NovaReservaPage } from './pages/reservas/NovaReservaPage';
import { ReservaDetailPage } from './pages/reservas/ReservaDetailPage';
import { AdminReservasPage } from './pages/admin/AdminReservasPage';
import { GestaoReservasPage, ReservasListPage } from './pages/reservas/ReservasListPage';
import { SalaDetailPage } from './pages/salas/SalaDetailPage';
import { SalaFormPage, SalasListPage } from './pages/salas/SalasPages';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route index element={<DashboardPage />} />

              <Route element={<ProtectedRoute roles={['ADMIN_SALA']} />}>
                <Route path="salas" element={<SalasListPage />} />
                <Route path="salas/nova" element={<SalaFormPage />} />
                <Route path="salas/:id" element={<SalaDetailPage />} />
                <Route path="layouts" element={<LayoutsPage />} />
                <Route path="posicoes" element={<PosicoesPage />} />
                <Route path="equipamentos" element={<EquipamentosPage />} />
                <Route path="regras-disponibilidade" element={<RegrasDisponibilidadePage />} />
                <Route path="usuarios" element={<UsuariosPage />} />
                <Route path="ia-execucoes" element={<IaExecucoesPage />} />
                <Route path="admin/reservas" element={<AdminReservasPage />} />
                <Route path="admin/reservas/:id" element={<ReservaDetailPage />} />
              </Route>

              <Route element={<ProtectedRoute roles={['USUARIO_FINAL']} />}>
                <Route path="minhas-equipes" element={<MinhasEquipesPage />} />
                <Route path="minhas-equipes/:id" element={<MinhaEquipeDetailPage />} />
              </Route>

              <Route element={<ProtectedRoute roles={['GESTOR_RESERVAS', 'ADMIN_SALA']} />}>
                <Route path="equipes" element={<EquipesListPage />} />
                <Route path="equipes/nova" element={<EquipeFormPage />} />
                <Route path="equipes/:id" element={<EquipeDetailPage />} />
              </Route>

              <Route element={<ProtectedRoute roles={['USUARIO_FINAL', 'GESTOR_RESERVAS', 'INTEGRADOR']} />}>
                <Route path="reservas" element={<ReservasListPage />} />
                <Route path="reservas/nova" element={<NovaReservaPage />} />
                <Route path="reservas/:id" element={<ReservaDetailPage />} />
              </Route>

              <Route
                element={
                  <ProtectedRoute
                    roles={['ADMIN_SALA', 'USUARIO_FINAL', 'GESTOR_RESERVAS', 'INTEGRADOR']}
                  />
                }
              >
                <Route path="disponibilidade" element={<DisponibilidadePage />} />
              </Route>

              <Route element={<ProtectedRoute roles={['GESTOR_RESERVAS']} />}>
                <Route path="reservas/gestao" element={<GestaoReservasPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
