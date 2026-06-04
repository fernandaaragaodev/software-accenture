package com.accenture.officehub_v1.security;

import com.accenture.officehub_v1.entity.Perfil;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.UsuarioPerfil;
import com.accenture.officehub_v1.repository.UsuarioPerfilRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new UsernameNotFoundException("Usuário inativo.");
        }

        List<SimpleGrantedAuthority> authorities = usuarioPerfilRepository.findByUsuarioId(usuario.getId()).stream()
                .map(UsuarioPerfil::getPerfil)
                .map(Perfil::getNome)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new UsuarioAutenticado(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getSenhaHash(),
                true,
                authorities);
    }
}
