import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from '../components/auth/ProtectedRoute';
import { RoleGuard } from '../components/auth/RoleGuard';
import { AppLayout } from '../layouts/AppLayout';
import { DashboardPage } from '../pages/DashboardPage';
import { DisponibilidadePage } from '../pages/DisponibilidadePage';
import { EquipamentosPage } from '../pages/EquipamentosPage';
import { LayoutSalaPage } from '../pages/LayoutSalaPage';
import { LoginPage } from '../pages/LoginPage';
import { NotificacoesPage } from '../pages/NotificacoesPage';
import { NovaReservaPage } from '../pages/NovaReservaPage';
import { PosicoesPage } from '../pages/PosicoesPage';
import { ReservaDetalhePage } from '../pages/ReservaDetalhePage';
import { ReservasPage } from '../pages/ReservasPage';
import { SalaDetalhePage } from '../pages/SalaDetalhePage';
import { SalaFormPage } from '../pages/SalaFormPage';
import { SalasPage } from '../pages/SalasPage';
import { ROLES } from '../utils/permissions';

export function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />

            <Route
              path="/salas"
              element={
                <RoleGuard allowed={[ROLES.ADMIN_SALA]}>
                  <SalasPage />
                </RoleGuard>
              }
            />
            <Route
              path="/disponibilidade"
              element={
                <RoleGuard
                  allowed={[
                    ROLES.ADMIN_SALA,
                    ROLES.USUARIO_FINAL,
                    ROLES.INTEGRADOR,
                    ROLES.GESTOR_RESERVAS,
                  ]}
                >
                  <DisponibilidadePage />
                </RoleGuard>
              }
            />
            <Route
              path="/salas/nova"
              element={
                <RoleGuard allowed={[ROLES.ADMIN_SALA]}>
                  <SalaFormPage />
                </RoleGuard>
              }
            />
            <Route
              path="/salas/:id"
              element={
                <RoleGuard
                  allowed={[
                    ROLES.ADMIN_SALA,
                    ROLES.USUARIO_FINAL,
                    ROLES.INTEGRADOR,
                    ROLES.GESTOR_RESERVAS,
                  ]}
                >
                  <SalaDetalhePage />
                </RoleGuard>
              }
            />
            <Route
              path="/salas/:id/editar"
              element={
                <RoleGuard allowed={[ROLES.ADMIN_SALA]}>
                  <SalaFormPage />
                </RoleGuard>
              }
            />
            <Route
              path="/salas/:id/layout"
              element={
                <RoleGuard allowed={[ROLES.ADMIN_SALA]}>
                  <LayoutSalaPage />
                </RoleGuard>
              }
            />
            <Route
              path="/salas/:id/disponibilidade"
              element={
                <RoleGuard
                  allowed={[
                    ROLES.ADMIN_SALA,
                    ROLES.USUARIO_FINAL,
                    ROLES.INTEGRADOR,
                    ROLES.GESTOR_RESERVAS,
                  ]}
                >
                  <DisponibilidadePage />
                </RoleGuard>
              }
            />

            <Route
              path="/posicoes"
              element={
                <RoleGuard allowed={[ROLES.ADMIN_SALA]}>
                  <PosicoesPage />
                </RoleGuard>
              }
            />
            <Route
              path="/equipamentos"
              element={
                <RoleGuard allowed={[ROLES.ADMIN_SALA]}>
                  <EquipamentosPage />
                </RoleGuard>
              }
            />

            <Route path="/reservas" element={<ReservasPage />} />
            <Route
              path="/reservas/nova"
              element={
                <RoleGuard
                  allowed={[
                    ROLES.USUARIO_FINAL,
                    ROLES.INTEGRADOR,
                    ROLES.GESTOR_RESERVAS,
                  ]}
                >
                  <NovaReservaPage />
                </RoleGuard>
              }
            />
            <Route path="/reservas/:id" element={<ReservaDetalhePage />} />

            <Route
              path="/notificacoes"
              element={
                <RoleGuard allowed={[ROLES.GESTOR_RESERVAS]}>
                  <NotificacoesPage />
                </RoleGuard>
              }
            />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
